package com.example.pocketmanage

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.data.CategoryEntry
import com.example.pocketmanage.databinding.ItemCategoryEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CategoryEntryAdapter(
    private val onItemClick: (CategoryEntry) -> Unit,
) : ListAdapter<CategoryEntry, CategoryEntryAdapter.VH>(DIFF) {

    var selectedId: Long? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCategoryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val binding: ItemCategoryEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
        }

        fun bind(entry: CategoryEntry) {
            val ctx = binding.root.context
            binding.entryCategory.text = entry.categoryName
            binding.entryDates.text = ctx.getString(
                R.string.category_entry_date_line,
                formatUtcMillis(entry.startDateMillis),
                formatUtcMillis(entry.endDateMillis),
            )
            binding.entryDescription.text = entry.description.ifBlank { "—" }

            val path = entry.photoPath
            if (!path.isNullOrBlank()) {
                val bmp = BitmapFactory.decodeFile(path)
                if (bmp != null) {
                    binding.entryPhoto.setImageBitmap(bmp)
                } else {
                    binding.entryPhoto.setImageDrawable(null)
                }
            } else {
                binding.entryPhoto.setImageDrawable(null)
            }

            val selected = entry.id == selectedId
            val stroke = ContextCompat.getColor(ctx, if (selected) R.color.primary else R.color.divider)
            binding.entryCard.strokeWidth = if (selected) (2 * ctx.resources.displayMetrics.density).toInt() else 0
            binding.entryCard.strokeColor = stroke
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryEntry>() {
            override fun areItemsTheSame(oldItem: CategoryEntry, newItem: CategoryEntry): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: CategoryEntry, newItem: CategoryEntry): Boolean =
                oldItem == newItem
        }

        private fun formatUtcMillis(millis: Long): String {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date(millis))
        }
    }
}
