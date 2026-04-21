package com.example.pocketmanage.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pocketmanage.R
import com.example.pocketmanage.data.FinanceAccount
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.databinding.DialogAddTransactionBinding
import com.example.pocketmanage.util.MoneyFormat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

object AddTransactionDialog {
    private const val STEP_RANDS = 100L
    private const val AMOUNT_MIN_RANDS = 100L
    private const val AMOUNT_MAX_RANDS = 50_000L

    fun show(activity: AppCompatActivity, onSaved: () -> Unit) {
        activity.lifecycleScope.launch {
            FinanceRepository.ensureSeeded()
            val accounts = FinanceRepository.accounts()
            val categories = FinanceRepository.budgetCategoryNames(activity)
            activity.runOnUiThread {
                showDialog(activity, accounts, categories, onSaved)
            }
        }
    }

    private fun randToCents(value: Float): Long =
        (value.toDouble() * 100.0).roundToLong()

    private fun minMaxRands(binding: DialogAddTransactionBinding): Pair<Long, Long> =
        if (binding.switchAmountLimits.isChecked) {
            binding.sliderMinLimit.value.roundToLong() to binding.sliderMaxLimit.value.roundToLong()
        } else {
            AMOUNT_MIN_RANDS to AMOUNT_MAX_RANDS
        }

    /** Keeps amount on R100 steps within current min/max. */
    private fun coerceAmountToBounds(binding: DialogAddTransactionBinding, rands: Long): Long {
        val (minR, maxR) = minMaxRands(binding)
        val clamped = rands.coerceIn(minR, maxR)
        val stepsDown = (clamped - minR) / STEP_RANDS
        return (minR + stepsDown * STEP_RANDS).coerceIn(minR, maxR)
    }

    private fun updateAmountUi(binding: DialogAddTransactionBinding, amountRands: Long) {
        binding.textAmountValue.text = MoneyFormat.formatCents(amountRands * 100)
        val (minR, maxR) = minMaxRands(binding)
        binding.buttonAmountDecrease.isEnabled = amountRands > minR
        binding.buttonAmountIncrease.isEnabled = amountRands < maxR
    }

    private fun updateLimitLabels(binding: DialogAddTransactionBinding) {
        binding.textMinLimitValue.text = MoneyFormat.formatCents(randToCents(binding.sliderMinLimit.value))
        binding.textMaxLimitValue.text = MoneyFormat.formatCents(randToCents(binding.sliderMaxLimit.value))
    }

    private fun showDialog(
        activity: AppCompatActivity,
        accounts: List<FinanceAccount>,
        categories: List<String>,
        onSaved: () -> Unit,
    ) {
        if (accounts.isEmpty()) {
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.validation_account_category,
                Snackbar.LENGTH_LONG,
            ).show()
            return
        }
        val binding = DialogAddTransactionBinding.inflate(LayoutInflater.from(activity))
        var amountRands = 5_000L

        fun refreshAmount() {
            amountRands = coerceAmountToBounds(binding, amountRands)
            updateAmountUi(binding, amountRands)
        }

        val accLabels = accounts.map { "${it.name} (#${it.id})" }
        binding.dropdownAccount.setAdapter(
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, accLabels),
        )
        binding.dropdownAccount.setText(accLabels[0], false)
        binding.dropdownCategory.setAdapter(
            ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, categories),
        )
        if (categories.isNotEmpty()) {
            binding.dropdownCategory.setText(categories[0], false)
        }

        binding.buttonAmountDecrease.setOnClickListener {
            val (minR, _) = minMaxRands(binding)
            if (amountRands > minR) {
                amountRands -= STEP_RANDS
                refreshAmount()
            }
        }
        binding.buttonAmountIncrease.setOnClickListener {
            val (_, maxR) = minMaxRands(binding)
            if (amountRands < maxR) {
                amountRands += STEP_RANDS
                refreshAmount()
            }
        }

        binding.sliderMinLimit.addOnChangeListener { _, _, _ ->
            updateLimitLabels(binding)
            if (binding.switchAmountLimits.isChecked) {
                refreshAmount()
            }
        }
        binding.sliderMaxLimit.addOnChangeListener { _, _, _ ->
            updateLimitLabels(binding)
            if (binding.switchAmountLimits.isChecked) {
                refreshAmount()
            }
        }
        binding.switchAmountLimits.setOnCheckedChangeListener { _, isChecked ->
            binding.limitsSection.visibility = if (isChecked) View.VISIBLE else View.GONE
            refreshAmount()
        }

        refreshAmount()
        updateLimitLabels(binding)

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.add_transaction)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val cents = amountRands * 100
                if (cents <= 0) {
                    Snackbar.make(binding.root, R.string.validation_amount, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (binding.switchAmountLimits.isChecked) {
                    val minC = randToCents(binding.sliderMinLimit.value)
                    val maxC = randToCents(binding.sliderMaxLimit.value)
                    if (cents < minC || cents > maxC) {
                        Snackbar.make(binding.root, R.string.validation_amount_limits, Snackbar.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
                val accText = binding.dropdownAccount.text?.toString().orEmpty()
                val idMatch = Regex("#(\\d+)").find(accText)
                val accountId = idMatch?.groupValues?.get(1)?.toLongOrNull()
                val category = binding.dropdownCategory.text?.toString()?.trim().orEmpty()
                if (accountId == null || category.isEmpty()) {
                    Snackbar.make(binding.root, R.string.validation_account_category, Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val isExpense = binding.radioExpense.isChecked
                val note = binding.inputNote.text?.toString().orEmpty()
                activity.lifecycleScope.launch {
                    try {
                        FinanceRepository.addTransaction(
                            accountId = accountId,
                            amountCents = cents,
                            category = category,
                            note = note,
                            isExpense = isExpense,
                        )
                        dialog.dismiss()
                        onSaved()
                    } catch (e: Exception) {
                        Snackbar.make(
                            binding.root,
                            e.message ?: activity.getString(R.string.error_transaction),
                            Snackbar.LENGTH_LONG,
                        ).show()
                    }
                }
            }
        }
        dialog.show()
    }
}
