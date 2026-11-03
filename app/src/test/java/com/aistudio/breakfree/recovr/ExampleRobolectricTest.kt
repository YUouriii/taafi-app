package com.aistudio.breakfree.recovr

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.breakfree.recovr.ai.PauseMessages
import com.aistudio.breakfree.recovr.data.BreakFreeRepository
import com.aistudio.breakfree.recovr.model.AppLanguage
import com.aistudio.breakfree.recovr.model.DailyCheckIn
import com.aistudio.breakfree.recovr.model.PauseOutcome
import com.aistudio.breakfree.recovr.model.PauseSession
import com.aistudio.breakfree.recovr.model.UserProfile
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BreakFree", appName)
  }

  @Test
  fun `repository preserves streak state and saves`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    val initial = repo.getStreakState()
    assertNotNull(initial)

    val saves = repo.recordGracePeriodSave()
    assertEquals(initial.gracePeriodSavesCount + 1, saves)
  }

  @Test
  fun `fresh install reports an honest zero streak`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val state = BreakFreeRepository(context).getStreakState()

    assertEquals(0L, state.longestStreakSeconds)
    assertEquals(0, state.gracePeriodSavesCount)
    assertEquals(0, state.sosCompletedCount)
    assertEquals(0, state.relapsesCount)
    assertTrue(
      "A new install must not start with invented progress",
      state.totalSecondsClean < 60L
    )
  }

  @Test
  fun `community keeps no local posts and remembers only own fist bumps`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    // The feed is cloud-only, so a fresh install has no posts of its own to show.
    assertTrue(repo.getCheeredPostIds().isEmpty())

    repo.setPostCheered("post-1", true)
    assertTrue(repo.getCheeredPostIds().contains("post-1"))

    repo.setPostCheered("post-1", false)
    assertTrue(repo.getCheeredPostIds().isEmpty())
  }

  @Test
  fun `a fresh install has no AI provider configured`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val config = BreakFreeRepository(context).getAiConfig()

    assertFalse("Without a key the counselor must stay offline", config.isConfigured)
    assertEquals("gemini-3.5-flash-lite", config.effectiveModel)
  }

  @Test
  fun `check-ins are stored locally and survive a new repository instance`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    val checkIn = DailyCheckIn(
      id = "2026-10-28",
      userId = "local",
      dateString = "2026-10-28",
      moodScore = 4,
      urgeIntensity = 3,
      primaryEmotions = listOf("Calm"),
      triggersEncountered = listOf("Work Stress"),
      reflectionNotes = "Stayed grounded.",
      streakProtected = true,
      createdAt = Timestamp(1_700_000_000L, 0)
    )
    repo.saveCheckIns("local", listOf(checkIn))

    val reloaded = BreakFreeRepository(context).getCheckIns("local")
    assertEquals(1, reloaded.size)
    val restored = reloaded.first()
    assertEquals(4, restored.moodScore)
    assertEquals(3, restored.urgeIntensity)
    assertEquals(listOf("Calm"), restored.primaryEmotions)
    assertEquals("Stayed grounded.", restored.reflectionNotes)
    assertEquals(1_700_000_000L, restored.createdAt?.seconds)
  }

  @Test
  fun `check-ins are scoped per account so signing out cannot leak them`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    repo.saveCheckIns(
      "userA",
      listOf(DailyCheckIn(id = "a", userId = "userA", dateString = "2026-10-27"))
    )

    assertTrue(repo.getCheckIns("local").isEmpty())
    assertTrue(repo.getCheckIns("userB").isEmpty())
    assertEquals(1, repo.getCheckIns("userA").size)
  }

  @Test
  fun `failed uploads are remembered per account until they succeed`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    repo.markPendingSync("userA", "2026-10-28")
    assertTrue(repo.getPendingSyncDates("userA").contains("2026-10-28"))
    assertTrue(repo.getPendingSyncDates("userB").isEmpty())

    repo.clearPendingSync("userA", "2026-10-28")
    assertTrue(repo.getPendingSyncDates("userA").isEmpty())
  }

  @Test
  fun `a fresh install adopts the cloud streak instead of overwriting it`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)
    val cloudStart = System.currentTimeMillis() - 3L * 24 * 3600 * 1000

    val localIsAhead = repo.applyCloudProfile(
      UserProfile(userId = "u1", cleanSinceTimestamp = cloudStart, relapsesCount = 0)
    )

    assertFalse("A device with no history must not push its zero streak", localIsAhead)
    val state = repo.getStreakState()
    assertEquals(cloudStart, state.cleanSinceTimestamp)
    assertTrue(state.totalSecondsClean >= 3L * 24 * 3600 - 60L)
  }

  @Test
  fun `a local relapse is never undone by a longer cloud streak`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    // Relapse recorded offline: the local clock restarted, the cloud has not heard yet.
    repo.resetStreakDueToRelapse()

    val localIsAhead = repo.applyCloudProfile(
      UserProfile(
        userId = "u1",
        cleanSinceTimestamp = System.currentTimeMillis() - 10L * 24 * 3600 * 1000,
        longestStreakSeconds = 10L * 24 * 3600,
        relapsesCount = 0
      )
    )

    assertTrue("The local reset is newer information and must be pushed", localIsAhead)
    assertTrue("The streak must stay reset", repo.getStreakState().totalSecondsClean < 60L)
  }

  @Test
  fun `pause sessions are stored locally and only survived waves are counted`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = BreakFreeRepository(context)

    assertEquals(0, repo.getUrgesSurvivedCount())

    repo.recordPauseSession(PauseSession("a", 1_000L, 600_000L, PauseOutcome.SURVIVED))
    repo.recordPauseSession(PauseSession("b", 2_000L, 600_000L, PauseOutcome.ENDED))
    repo.recordPauseSession(PauseSession("c", 3_000L, 600_000L, PauseOutcome.SURVIVED))

    assertEquals(3, repo.getPauseSessions().size)
    assertEquals(2, repo.getUrgesSurvivedCount())
    assertEquals(PauseOutcome.SURVIVED, repo.getPauseSessions().first().outcome)
  }

  @Test
  fun `my promise survives a new repository instance`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    BreakFreeRepository(context).saveMyPromise("I want to be present for my daughter")

    assertEquals(
      "I want to be present for my daughter",
      BreakFreeRepository(context).getMyPromise()
    )
  }

  @Test
  fun `pause templates always produce three localised notifications`() {
    val english = PauseMessages.templates("Stay present", 14, 30, 5, AppLanguage.ENGLISH)
    val arabic = PauseMessages.templates("", 14, 30, 5, AppLanguage.ARABIC)
    val french = PauseMessages.templates("Rester présent", 14, 30, 5, AppLanguage.FRENCH)

    assertEquals(3, english.size)
    assertEquals(3, arabic.size)
    assertEquals(3, french.size)
    assertTrue(english.first().contains("14"))
    assertTrue(french.first().contains("14"))
    assertTrue("No promise falls back to the streak line", arabic.first().contains("14"))
  }

  @Test
  fun `model output is cleaned into at most three notification lines`() {
    val raw = """
      1. "You wrote: stay present." 
      - 14 days in, this wave passes.
      • 5 urges already surfed.
      4. A fourth line that must be dropped.
    """.trimIndent()

    val parsed = PauseMessages.parse(raw)
    assertEquals(3, parsed.size)
    assertEquals("You wrote: stay present.", parsed[0])
    assertEquals("14 days in, this wave passes.", parsed[1])
  }
}
