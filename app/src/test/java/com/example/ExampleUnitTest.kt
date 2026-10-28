package com.example

import com.example.model.BadgesRepository
import com.example.model.DailyCheckIn
import com.example.model.GracePeriodState
import com.example.model.StreakState
import com.example.monitoring.ExplicitContentDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStreakStateCalculations() {
        val now = 1_000_000_000L * 1000L
        val cleanSince = now - (5L * 86400L * 1000L + 3L * 3600L * 1000L + 15L * 60L * 1000L + 20L * 1000L)

        val state = StreakState(
            cleanSinceTimestamp = cleanSince,
            currentTimestamp = now,
            gracePeriodSavesCount = 2
        )

        assertEquals(5L, state.days)
        assertEquals(3L, state.hours)
        assertEquals(15L, state.minutes)
        assertEquals(20L, state.seconds)
        assertTrue("Dopamine recovery score should be between 0 and 100", state.dopamineRecoveryScore in 0..100)
    }

    @Test
    fun testDailyCheckInModel() {
        val checkIn = DailyCheckIn(
            id = "2026-10-28",
            userId = "user_123",
            dateString = "2026-10-28",
            moodScore = 5,
            urgeIntensity = 8,
            primaryEmotions = listOf("Motivated", "Grateful"),
            triggersEncountered = listOf("Late Night"),
            reflectionNotes = "Had a spike late night but conquered it.",
            streakProtected = true
        )

        assertEquals("🔥", checkIn.moodEmoji)
        assertEquals(0xFFF43F5E, checkIn.urgeColorHex)
        assertTrue(checkIn.streakProtected)
    }

    @Test
    fun testExplicitContentDetector() {
        val domainCheck = ExplicitContentDetector.isExplicitContent("https://www.pornhub.com/video")
        assertTrue(domainCheck.first)
        assertEquals("pornhub", domainCheck.second)

        val kwCheck = ExplicitContentDetector.isExplicitContent("free adult video nsfw")
        assertTrue(kwCheck.first)

        val arabicCheck = ExplicitContentDetector.isExplicitContent("مقاطع جنس وأفلام إباحية")
        assertTrue(arabicCheck.first)

        val safeCheck = ExplicitContentDetector.isExplicitContent("https://www.wikipedia.org/wiki/Dopamine")
        assertFalse(safeCheck.first)
    }

    @Test
    fun testBadgesUnlock() {
        val earlyBadges = BadgesRepository.getAllBadges(streakDays = 0, graceSaves = 0, sosCompleted = 0)
        val sparkBadge = earlyBadges.first { it.id == "spark" }
        assertFalse(sparkBadge.unlocked)

        val weekBadges = BadgesRepository.getAllBadges(streakDays = 7, graceSaves = 1, sosCompleted = 1)
        val fortressBadge = weekBadges.first { it.id == "fortress" }
        val graceBadge = weekBadges.first { it.id == "grace_warrior" }
        val zenBadge = weekBadges.first { it.id == "zen_breather" }

        assertTrue("7-day fortress badge should be unlocked", fortressBadge.unlocked)
        assertTrue("Grace warrior badge should be unlocked", graceBadge.unlocked)
        assertTrue("Zen breather badge should be unlocked", zenBadge.unlocked)
    }

    @Test
    fun testGracePeriodState() {
        val state = GracePeriodState(
            isGraceActive = true,
            remainingSeconds = 60,
            totalSeconds = 120
        )
        assertEquals(0.5f, state.progress, 0.01f)
        assertEquals("01:00", state.formattedTime)
    }
}
