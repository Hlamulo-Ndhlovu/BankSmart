package com.example.pocketmanage.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.R
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.databinding.ItemAnalysisCategoryBinding
import kotlin.math.min
import kotlin.math.roundToInt

class AnalysisCategoryAdapter : RecyclerView.Adapter<AnalysisCategoryAdapter.VH>() {

    private var items: List<FinanceRepository.BudgetRowUi> = emptyList()

    fun submit(list: List<FinanceRepository.BudgetRowUi>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAnalysisCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemAnalysisCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: FinanceRepository.BudgetRowUi) {
            binding.analysisCategoryName.text = row.name
            val limit = row.limitCents
            val spent = row.spentCents
            val pct = if (limit > 0) {
                min(100, ((spent * 100f) / limit).roundToInt())
            } else {
                0
            }
            binding.analysisProgress.progress = pct
            if (limit > 0 && spent > limit) {
                binding.analysisProgress.progressTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.accent_red),
                )
            } else {
                binding.analysisProgress.progressTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(R.color.primary),
                )
            }
        }
    }
}
