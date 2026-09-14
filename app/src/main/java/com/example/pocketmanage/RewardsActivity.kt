package com.example.pocketmanage

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.data.FinanceRepository
import com.example.pocketmanage.databinding.ActivityRewardsBinding
import com.example.pocketmanage.gamification.LoyaltyStatus
import com.example.pocketmanage.gamification.LoyaltyTier
import com.example.pocketmanage.ui.RewardAdapter
import kotlinx.coroutines.launch

class RewardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardsBinding
    private val rewardAdapter = RewardAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rewardsToolbar.setNavigationOnClickListener { finish() }
        binding.rewardsRecycler.layoutManager = LinearLayoutManager(this)
        binding.rewardsRecycler.adapter = rewardAdapter
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.requireSignedIn(this)
        lifecycleScope.launch {
            FinanceRepository.ensureSeeded()
            val rewards = FinanceRepository.rewardsStatus()
            bind(LoyaltyStatus.fromPoints(rewards.points))
        }
    }

    private fun bind(status: LoyaltyStatus) {
        val tier = status.tier
        val tierColor = Color.parseColor(tier.colorHex)
        val tierName = getString(tier.nameRes)

        binding.tierBadgeText.text = tierName.take(1)
        binding.tierBadgeText.background.mutate().setTint(tierColor)
        binding.tierNameText.text = tierName
        binding.tierNameText.setTextColor(tierColor)
        binding.tierDescriptionText.text = getString(tier.descriptionRes)
        binding.tierPointsText.text = getString(R.string.rewards_points_fmt, status.points)
        binding.tierProgressBar.progress = status.tierProgressPercent
        binding.tierProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(tierColor)

        val next = status.nextTier
        binding.tierNextText.text = if (next == null) {
            getString(R.string.loyalty_top_tier)
        } else {
            getString(R.string.loyalty_next_tier_fmt, status.pointsToNextTier, getString(next.nameRes))
        }

        bindLadder(tier)
        bindPerks(tier)

        binding.rewardsHeader.text = getString(
            R.string.loyalty_rewards_header_fmt,
            status.unlockedRewards.size,
            status.unlockedRewards.size + status.lockedRewards.size,
        )
        rewardAdapter.submit(status.unlockedRewards, status.lockedRewards)
    }

    private fun bindLadder(current: LoyaltyTier) {
        val ladder = binding.tierLadder
        ladder.removeAllViews()
        val inactive = ContextCompat.getColor(this, R.color.divider)
        LoyaltyTier.entries.forEach { tier ->
            val reached = tier.ordinal <= current.ordinal
            val color = if (reached) Color.parseColor(tier.colorHex) else inactive
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(14), dp(14))
                background = ContextCompat.getDrawable(this@RewardsActivity, R.drawable.tier_badge_circle)
                    ?.mutate()
                    ?.also { it.setTint(color) }
            }
            val label = TextView(this).apply {
                text = getString(tier.nameRes)
                textSize = 10f
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
                setTextColor(
                    ContextCompat.getColor(
                        this@RewardsActivity,
                        if (reached) R.color.text_primary else R.color.text_secondary,
                    ),
                )
                if (tier == current) setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            column.addView(dot)
            column.addView(label)
            ladder.addView(column)
        }
    }

    private fun bindPerks(tier: LoyaltyTier) {
        val container = binding.perksContainer
        container.removeAllViews()
        LoyaltyTier.entries
            .filter { it.ordinal <= tier.ordinal }
            .forEach { t ->
                t.perkRes.forEach { perk ->
                    container.addView(perkRow(getString(perk)))
                }
            }
    }

    private fun perkRow(text: String): TextView = TextView(this).apply {
        this.text = getString(R.string.loyalty_perk_row_fmt, text)
        textSize = 14f
        setPadding(0, dp(8), 0, dp(8))
        setTextColor(ContextCompat.getColor(this@RewardsActivity, R.color.text_primary))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
