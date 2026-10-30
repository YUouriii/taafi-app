package com.aistudio.breakfree.recovr.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fires on the release date (and is re-checked on every app start) so the 90-day
 * commitment ends without the user having to remember it.
 */
class UninstallLockReleaseReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        UninstallLock.releaseIfDue(context)
    }
}
