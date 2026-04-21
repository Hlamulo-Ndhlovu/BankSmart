package com.example.pocketmanage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.util.MoneyFormat
import com.google.android.material.textfield.TextInputEditText
import com.example.pocketmanage.ui.AccountAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AccountsActivity : AppCompatActivity() {

    private val adapter = AccountAdapter { accountId, balanceCents ->
        lifecycleScope.launch {
            FinanceRepository.updateAccountBalance(accountId, balanceCents)
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_accounts)

        val mainView = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.accountsRecycler).apply {
            layoutManager = LinearLayoutManager(this@AccountsActivity)
            adapter = this@AccountsActivity.adapter
            isNestedScrollingEnabled = false
        }

        findViewById<MaterialButton>(R.id.addNewAccountButton).setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null, false)
            val nameEdit = dialogView.findViewById<TextInputEditText>(R.id.dialogAccountName)
            val balanceEdit = dialogView.findViewById<TextInputEditText>(R.id.dialogAccountBalance)
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_account_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
            dialog.setOnShowListener {
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = nameEdit.text?.toString()?.trim().orEmpty()
                    if (name.isEmpty()) {
                        Snackbar.make(mainView, R.string.validation_account_name, Snackbar.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val balanceCents = MoneyFormat.parseRandToCents(balanceEdit.text?.toString().orEmpty()) ?: 0L
                    lifecycleScope.launch {
                        FinanceRepository.addAccount(name, balanceCents)
                        refresh()
                    }
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_accounts
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_accounts -> true
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoriesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.requireSignedIn(this)
        refresh()
    }

    private fun refresh() {
        lifecycleScope.launch {
            FinanceRepository.ensureSeeded()
            findViewById<android.widget.TextView>(R.id.allAccountsTotalText).text =
                getString(R.string.all_accounts_heading) + " " + FinanceRepository.accountsTotalFormatted()
            val (inc, exp) = FinanceRepository.monthIncomeExpenseCents()
            findViewById<android.widget.TextView>(R.id.accountsExpenseSoFar).text =
                MoneyFormat.formatCents(exp)
            findViewById<android.widget.TextView>(R.id.accountsIncomeSoFar).text =
                MoneyFormat.formatCents(inc)
            adapter.submit(FinanceRepository.accounts())
        }
    }
}
