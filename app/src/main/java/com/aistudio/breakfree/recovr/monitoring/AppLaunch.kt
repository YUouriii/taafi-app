package com.aistudio.breakfree.recovr.monitoring

import android.content.Context
import android.content.Intent
import com.aistudio.breakfree.recovr.MainActivity

/**
 * Opens the app through whichever launcher component is enabled right now.
 *
 * While Stealth Mode is on, [MainActivity] is disabled and the enabled activity-alias
 * (Calculator / Notes / Weather) is the only valid entry point, so an explicit
 * `Intent(context, MainActivity::class.java)` would not launch anything. Resolving the
 * package's current launcher intent keeps notification taps working in both modes.
 */
internal fun appLaunchIntent(context: Context): Intent {
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: Intent(context, MainActivity::class.java)
    launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    return launch
}
