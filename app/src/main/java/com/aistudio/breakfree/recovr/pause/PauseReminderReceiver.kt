package com.aistudio.breakfree.recovr.pause

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Fires the 3/6/9-minute pause notification, even if the app process is gone. */
class PauseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra(PauseNotifier.EXTRA_MESSAGE) ?: return
        val id = intent.getIntExtra(PauseNotifier.EXTRA_NOTIFICATION_ID, 7301)
        Log.i("PauseNotifier", "Pause reminder delivered (id=$id)")
        PauseNotifier.show(context, message, id)
    }
}
