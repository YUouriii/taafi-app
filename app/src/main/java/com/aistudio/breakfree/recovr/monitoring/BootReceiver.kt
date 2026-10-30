package com.aistudio.breakfree.recovr.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Starts the protection services after the device boots, so the monitor and the DNS
 * filter come back without the user having to open the app first.
 *
 * ## OEM note
 *
 * Xiaomi (MIUI/HyperOS) and some other OEMs block autostart by default. On those phones
 * the user must enable "Autostart" for BreakFree in Settings → Apps → Manage apps →
 * BreakFree. The setup checklist tells them so.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return
        Log.i("BootReceiver", "Boot completed; starting protection services")
        GuardForegroundService.start(context)
        // The accessibility service is re-enabled by the system if it was on; nothing to
        // do here. The DNS filter is restarted by MainActivity.syncDnsShield() on the
        // next launch, and by GuardForegroundService keeping the process alive.
    }
}
