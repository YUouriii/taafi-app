package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BreakFreeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
