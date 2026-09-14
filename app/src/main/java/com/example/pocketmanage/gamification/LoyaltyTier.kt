package com.example.pocketmanage.gamification

import androidx.annotation.StringRes
import com.example.pocketmanage.R

/**
 * Loyalty tier structure. Tiers are unlocked by lifetime reward points and each
 * tier carries its own set of perks. Points are earned by the existing
 * gamification engine (see FinanceRepository.rewardsStatus()).
 */
enum class LoyaltyTier(
    val minPoints: Int,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val colorHex: String,
    @StringRes val perkRes: List<Int>,
) {
    BRONZE(
        minPoints = 0,
        nameRes = R.string.tier_bronze,
        descriptionRes = R.string.tier_bronze_desc,
        colorHex = "#CD7F32",
        perkRes = listOf(R.string.perk_bronze_1, R.string.perk_bronze_2),
    ),
    SILVER(
        minPoints = 200,
        nameRes = R.string.tier_silver,
        descriptionRes = R.string.tier_silver_desc,
        colorHex = "#9CA3AF",
        perkRes = listOf(R.string.perk_silver_1, R.string.perk_silver_2),
    ),
    GOLD(
        minPoints = 500,
        nameRes = R.string.tier_gold,
        descriptionRes = R.string.tier_gold_desc,
        colorHex = "#F59E0B",
        perkRes = listOf(R.string.perk_gold_1, R.string.perk_gold_2),
    ),
    PLATINUM(
        minPoints = 1000,
        nameRes = R.string.tier_platinum,
        descriptionRes = R.string.tier_platinum_desc,
        colorHex = "#60A5FA",
        perkRes = listOf(R.string.perk_platinum_1, R.string.perk_platinum_2),
    ),
    DIAMOND(
        minPoints = 2000,
        nameRes = R.string.tier_diamond,
        descriptionRes = R.string.tier_diamond_desc,
        colorHex = "#A78BFA",
        perkRes = listOf(R.string.perk_diamond_1, R.string.perk_diamond_2),
    );

    val next: LoyaltyTier?
        get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): LoyaltyTier =
            entries.last { points >= it.minPoints }
    }
}

/** A reward that becomes unlocked once the user reaches [requiredPoints]. */
data class Reward(
    val id: String,
    val requiredPoints: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
)

object RewardCatalog {
    val all: List<Reward> = listOf(
        Reward("first_steps", 10, R.string.reward_first_steps, R.string.reward_first_steps_desc),
        Reward("streak_starter", 50, R.string.reward_streak_starter, R.string.reward_streak_starter_desc),
        Reward("budget_keeper", 100, R.string.reward_budget_keeper, R.string.reward_budget_keeper_desc),
        Reward("silver_theme", 200, R.string.reward_silver_theme, R.string.reward_silver_theme_desc),
        Reward("saver_badge", 350, R.string.reward_saver_badge, R.string.reward_saver_badge_desc),
        Reward("gold_frame", 500, R.string.reward_gold_frame, R.string.reward_gold_frame_desc),
        Reward("insights_pro", 750, R.string.reward_insights_pro, R.string.reward_insights_pro_desc),
        Reward("platinum_card", 1000, R.string.reward_platinum_card, R.string.reward_platinum_card_desc),
        Reward("diamond_crown", 2000, R.string.reward_diamond_crown, R.string.reward_diamond_crown_desc),
    )
}

data class LoyaltyStatus(
    val points: Int,
    val tier: LoyaltyTier,
    val nextTier: LoyaltyTier?,
    val pointsToNextTier: Int,
    val tierProgressPercent: Int,
    val unlockedRewards: List<Reward>,
    val lockedRewards: List<Reward>,
) {
    companion object {
        fun fromPoints(points: Int): LoyaltyStatus {
            val safePoints = points.coerceAtLeast(0)
            val tier = LoyaltyTier.forPoints(safePoints)
            val next = tier.next
            val pointsToNext = next?.let { (it.minPoints - safePoints).coerceAtLeast(0) } ?: 0
            val progress = if (next == null) {
                100
            } else {
                val span = next.minPoints - tier.minPoints
                ((safePoints - tier.minPoints) * 100 / span).coerceIn(0, 100)
            }
            val (unlocked, locked) = RewardCatalog.all.partition { safePoints >= it.requiredPoints }
            return LoyaltyStatus(
                points = safePoints,
                tier = tier,
                nextTier = next,
                pointsToNextTier = pointsToNext,
                tierProgressPercent = progress,
                unlockedRewards = unlocked,
                lockedRewards = locked,
            )
        }
    }
}
