package com.example.pocketmanage

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.util.MoneyFormat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToLong

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analysis)

        val mainView = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_analysis
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> true
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
        lifecycleScope.launch {
            FinanceRepository.ensureSeeded()
            val (inc, exp) = FinanceRepository.monthIncomeExpenseCents()
            bindOverviewChart(findViewById(R.id.analysisOverviewChart), inc, exp)
            val rows = FinanceRepository.dashboardBudgetRows()
            bindCategoryChart(findViewById(R.id.analysisCategoryChart), rows)
        }
    }

    private fun applyBaseBarStyle(chart: BarChart) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                MoneyFormat.formatCents((value * 100f).roundToLong())
        }
    }

    private fun bindOverviewChart(chart: BarChart, incCents: Long, expCents: Long) {
        applyBaseBarStyle(chart)
        chart.setScaleEnabled(false)
        chart.setDragEnabled(false)
        val incR = incCents / 100f
        val expR = expCents / 100f
        val entries = listOf(
            BarEntry(0f, incR),
            BarEntry(1f, expR),
        )
        val set = BarDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(this@AnalysisActivity, R.color.accent),
                ContextCompat.getColor(this@AnalysisActivity, R.color.accent_red),
            )
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    MoneyFormat.formatCents((value * 100f).roundToLong())
            }
        }
        val maxR = max(incR, expR)
        chart.axisLeft.axisMaximum = if (maxR > 0f) maxR * 1.15f else 1f
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf(getString(R.string.income), getString(R.string.expenses)),
        )
        chart.xAxis.granularity = 1f
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = 1.5f
        chart.data = BarData(set).apply { barWidth = 0.45f }
        chart.invalidate()
    }

    private fun bindCategoryChart(chart: BarChart, rows: List<FinanceRepository.BudgetRowUi>) {
        applyBaseBarStyle(chart)
        chart.setDragEnabled(true)
        chart.setScaleEnabled(false)
        chart.setVisibleXRangeMaximum(8f)
        val palette = listOf(
            R.color.primary,
            R.color.accent,
            R.color.accent_red,
            R.color.primary_light,
        )
        val entries = rows.mapIndexed { i, row ->
            BarEntry(i.toFloat(), row.spentCents / 100f)
        }
        if (entries.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }
        val colors = rows.mapIndexed { i, _ ->
            ContextCompat.getColor(this, palette[i % palette.size])
        }
        val set = BarDataSet(entries, "").apply {
            this.colors = colors
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    MoneyFormat.formatCents((value * 100f).roundToLong())
            }
        }
        val labels = rows.map { row ->
            val n = row.name
            if (n.length <= 14) n else n.take(12) + "…"
        }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelRotationAngle = -35f
        chart.xAxis.granularity = 1f
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = (rows.size - 1).toFloat() + 0.5f
        chart.xAxis.setLabelCount(minOf(rows.size, 12), false)
        val maxR = rows.maxOfOrNull { it.spentCents }?.div(100f) ?: 0f
        chart.axisLeft.axisMaximum = if (maxR > 0f) maxR * 1.15f else 1f
        chart.data = BarData(set).apply { barWidth = 0.65f }
        chart.setExtraBottomOffset(16f)
        chart.invalidate()
    }
}
