package com.example.pocketmanage.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.R
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.databinding.ItemDashboardBudgetBinding
import com.example.pocketmanage.util.MoneyFormat
import kotlin.math.min
import kotlin.math.roundToInt

class DashboardBudgetAdapter : RecyclerView.Adapter<DashboardBudgetAdapter.VH>() {

    private var items: List<FinanceRepository.BudgetRowUi> = emptyList()

    private val iconColors = intArrayOf(
        Color.parseColor("#E0E7FF"),
        Color.parseColor("#FEF3C7"),
        Color.parseColor("#FCE7F3"),
        Color.parseColor("#D1FAE5"),
    )

    fun submit(list: List<FinanceRepository.BudgetRowUi>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDashboardBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemDashboardBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: FinanceRepository.BudgetRowUi) {
            binding.categoryName.text = row.name
            binding.categoryIcon.setBackgroundColor(iconColors[row.colorIndex])
            val spent = row.spentCents
            val limit = row.limitCents
            binding.categoryProgress.text = binding.root.context.getString(
                R.string.budget_spent_of_limit,
                MoneyFormat.formatCents(spent),
                MoneyFormat.formatCents(limit),
            )
            val pct = if (limit > 0) {
                min(100, ((spent * 100f) / limit).roundToInt())
            } else {
                0
            }
            binding.progressBar.progress = pct
            val remaining = (limit - spent).coerceAtLeast(0)
            binding.remainingAmount.text = MoneyFormat.formatCents(remaining)
            if (limit > 0 && spent > limit) {
                binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.accent_red),
                )
            } else {
                binding.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.primary),
                )
            }
        }
    }
}
