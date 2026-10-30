package com.aistudio.breakfree.recovr

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.breakfree.recovr.data.BreakFreeRepository
import com.aistudio.breakfree.recovr.model.DailyCheckIn
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
  fun `community starts empty instead of seeding fake users`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertTrue(BreakFreeRepository(context).getCommunityPosts().isEmpty())
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
}
