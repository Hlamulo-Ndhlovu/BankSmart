package com.example.pocketmanage.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.R
import com.example.pocketmanage.data.FinanceTransaction
import com.example.pocketmanage.databinding.ItemTransactionRowBinding
import com.example.pocketmanage.util.MoneyFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.VH>() {

    private var items: List<FinanceTransaction> = emptyList()
    private var accountNames: Map<Long, String> = emptyMap()
    private val dateFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun submit(transactions: List<FinanceTransaction>, names: Map<Long, String>) {
        items = transactions
        accountNames = names
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTransactionRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class VH(private val binding: ItemTransactionRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tx: FinanceTransaction) {
            binding.txCategory.text = tx.category
            val acc = accountNames[tx.accountId] ?: "—"
            val note = if (tx.note.isNotBlank()) " · ${tx.note}" else ""
            binding.txMeta.text = "${acc}${note} · ${dateFmt.format(Date(tx.dateMillis))}"
            val formatted = MoneyFormat.formatCents(tx.amountCents)
            binding.txAmount.text = formatted
            val color = if (tx.amountCents < 0) {
                ContextCompat.getColor(binding.root.context, R.color.accent_red)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.accent)
            }
            binding.txAmount.setTextColor(color)
        }
    }
}
