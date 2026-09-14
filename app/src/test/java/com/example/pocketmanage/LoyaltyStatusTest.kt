package com.example.pocketmanage

import com.example.pocketmanage.gamification.LoyaltyStatus
import com.example.pocketmanage.gamification.LoyaltyTier
import com.example.pocketmanage.gamification.RewardCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoyaltyStatusTest {

    @Test
    fun zeroPoints_isBronzeWithNothingUnlocked() {
        val status = LoyaltyStatus.fromPoints(0)
        assertEquals(LoyaltyTier.BRONZE, status.tier)
        assertEquals(LoyaltyTier.SILVER, status.nextTier)
        assertEquals(200, status.pointsToNextTier)
        assertEquals(0, status.tierProgressPercent)
        assertTrue(status.unlockedRewards.isEmpty())
        assertEquals(RewardCatalog.all.size, status.lockedRewards.size)
    }

    @Test
    fun negativePoints_areClampedToZero() {
        assertEquals(0, LoyaltyStatus.fromPoints(-50).points)
    }

    @Test
    fun tierBoundaries_areInclusive() {
        assertEquals(LoyaltyTier.BRONZE, LoyaltyTier.forPoints(199))
        assertEquals(LoyaltyTier.SILVER, LoyaltyTier.forPoints(200))
        assertEquals(LoyaltyTier.GOLD, LoyaltyTier.forPoints(500))
        assertEquals(LoyaltyTier.PLATINUM, LoyaltyTier.forPoints(1000))
        assertEquals(LoyaltyTier.DIAMOND, LoyaltyTier.forPoints(2000))
    }

    @Test
    fun midTier_progressIsProportional() {
        val status = LoyaltyStatus.fromPoints(350)
        assertEquals(LoyaltyTier.SILVER, status.tier)
        assertEquals(150, status.pointsToNextTier)
        assertEquals(50, status.tierProgressPercent)
    }

    @Test
    fun topTier_hasNoNextTierAndFullProgress() {
        val status = LoyaltyStatus.fromPoints(5000)
        assertEquals(LoyaltyTier.DIAMOND, status.tier)
        assertNull(status.nextTier)
        assertEquals(0, status.pointsToNextTier)
        assertEquals(100, status.tierProgressPercent)
        assertTrue(status.lockedRewards.isEmpty())
    }

    @Test
    fun rewards_unlockByThreshold() {
        val status = LoyaltyStatus.fromPoints(100)
        assertEquals(
            listOf("first_steps", "streak_starter", "budget_keeper"),
            status.unlockedRewards.map { it.id },
        )
    }

    @Test
    fun catalog_isSortedByRequiredPoints() {
        val points = RewardCatalog.all.map { it.requiredPoints }
        assertEquals(points.sorted(), points)
    }
}
