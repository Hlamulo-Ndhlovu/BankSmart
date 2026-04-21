package com.example.pocketmanage

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.databinding.ActivityMainBinding
import com.example.pocketmanage.ui.AddTransactionDialog
import com.example.pocketmanage.ui.DashboardBudgetAdapter
import com.example.pocketmanage.ui.TransactionAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val budgetAdapter = DashboardBudgetAdapter()
    private val transactionAdapter = TransactionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.budgetRecycler.layoutManager = LinearLayoutManager(this)
        binding.budgetRecycler.adapter = budgetAdapter
        binding.budgetRecycler.isNestedScrollingEnabled = false

        binding.recentTransactionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.recentTransactionsRecycler.adapter = transactionAdapter
        binding.recentTransactionsRecycler.isNestedScrollingEnabled = false

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        binding.addTransactionFab.setOnClickListener {
            AddTransactionDialog.show(this) {
                refreshDashboard()
            }
        }

        binding.bottomNav.selectedItemId = R.id.nav_dashboard
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_accounts -> {
                    startActivity(Intent(this, AccountsActivity::class.java))
                    finish()
                    true
                }
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
        refreshDashboard()
    }

    private fun refreshDashboard() {
        lifecycleScope.launch {
            FinanceRepository.ensureSeeded()
            binding.totalBalanceText.text = FinanceRepository.totalBalanceFormatted()
            val (inc, exp) = FinanceRepository.monthIncomeExpense()
            binding.incomeText.text = inc
            binding.expensesText.text = exp
            budgetAdapter.submit(FinanceRepository.dashboardBudgetRows())
            val txs = FinanceRepository.recentTransactions(25)
            val accounts = FinanceRepository.accounts()
            val nameMap = accounts.associateBy({ it.id }, { it.name })
            transactionAdapter.submit(txs, nameMap)
            val empty = txs.isEmpty()
            binding.emptyTransactionsText.visibility =
                if (empty) android.view.View.VISIBLE else android.view.View.GONE
            binding.recentTransactionsRecycler.visibility =
                if (empty) android.view.View.GONE else android.view.View.VISIBLE
        }
    }
}
