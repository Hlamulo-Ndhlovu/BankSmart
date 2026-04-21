package com.example.pocketmanage.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.R
import com.example.pocketmanage.data.FinanceAccount
import com.example.pocketmanage.databinding.ItemAccountRowBinding
import com.example.pocketmanage.util.MoneyFormat

class AccountAdapter(
    private val onBalanceCommitted: (accountId: Long, balanceCents: Long) -> Unit,
) : RecyclerView.Adapter<AccountAdapter.VH>() {

    private var items: List<FinanceAccount> = emptyList()

    private val iconBg = mapOf(
        "card" to Color.parseColor("#E0E7FF"),
        "cash" to Color.parseColor("#D1FAE5"),
        "paypal" to Color.parseColor("#FEF3C7"),
        "savings" to Color.parseColor("#FCE7F3"),
        "custom" to Color.parseColor("#E5E7EB"),
    )

    private val iconRes = mapOf(
        "card" to android.R.drawable.ic_menu_gallery,
        "cash" to android.R.drawable.ic_menu_myplaces,
        "paypal" to android.R.drawable.ic_menu_send,
        "savings" to android.R.drawable.ic_menu_compass,
        "custom" to android.R.drawable.ic_menu_add,
    )

    fun submit(list: List<FinanceAccount>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAccountRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemAccountRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(acc: FinanceAccount) {
            binding.accountName.text = acc.name
            val key = acc.iconKey.lowercase()
            binding.accountIconCard.setCardBackgroundColor(iconBg[key] ?: iconBg["custom"]!!)
            binding.accountIcon.setImageResource(iconRes[key] ?: iconRes["custom"]!!)

            if (!binding.accountBalanceEdit.hasFocus()) {
                binding.accountBalanceEdit.setText(MoneyFormat.centsToDecimalString(acc.balanceCents))
            }

            binding.accountBalanceEdit.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val current = items.getOrNull(pos) ?: return@setOnFocusChangeListener
                        commit(binding, current)
                    }
                }
            }
            binding.accountBalanceEdit.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    v.clearFocus()
                    true
                } else {
                    false
                }
            }
        }

        private fun commit(binding: ItemAccountRowBinding, acc: FinanceAccount) {
            val txt = binding.accountBalanceEdit.text?.toString().orEmpty()
            val cents = MoneyFormat.parseRandToCents(txt)
            if (cents == null) {
                binding.accountBalanceEdit.setText(MoneyFormat.centsToDecimalString(acc.balanceCents))
                return
            }
            if (cents != acc.balanceCents) {
                onBalanceCommitted(acc.id, cents)
            }
        }
    }
}
