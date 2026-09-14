package com.example.pocketmanage.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanage.R
import com.example.pocketmanage.databinding.ItemRewardBinding
import com.example.pocketmanage.gamification.Reward

class RewardAdapter : RecyclerView.Adapter<RewardAdapter.VH>() {

    private data class Row(val reward: Reward, val unlocked: Boolean)

    private val rows = mutableListOf<Row>()

    fun submit(unlocked: List<Reward>, locked: List<Reward>) {
        rows.clear()
        rows += unlocked.map { Row(it, true) }
        rows += locked.map { Row(it, false) }
        notifyDataSetChanged()
    }

    class VH(val binding: ItemRewardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (reward, unlocked) = rows[position]
        val ctx = holder.itemView.context
        val b = holder.binding
        b.rewardTitle.text = ctx.getString(reward.titleRes)
        b.rewardDescription.text = ctx.getString(reward.descriptionRes)
        b.rewardIcon.text = if (unlocked) "\u2605" else "\uD83D\uDD12"
        b.rewardIcon.background.mutate().setTint(
            if (unlocked) ContextCompat.getColor(ctx, R.color.accent) else Color.parseColor("#9CA3AF"),
        )
        b.rewardStatus.text = if (unlocked) {
            ctx.getString(R.string.reward_unlocked)
        } else {
            ctx.getString(R.string.reward_locked_fmt, reward.requiredPoints)
        }
        b.rewardStatus.setTextColor(
            ContextCompat.getColor(ctx, if (unlocked) R.color.accent else R.color.text_secondary),
        )
        b.root.alpha = if (unlocked) 1f else 0.7f
    }
}
