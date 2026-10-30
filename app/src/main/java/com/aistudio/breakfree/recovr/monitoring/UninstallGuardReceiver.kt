package com.aistudio.breakfree.recovr.monitoring

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.aistudio.breakfree.recovr.R

/**
 * The administrator component itself. It is intentionally empty: holding the role is the
 * whole point, because Android refuses to uninstall an active administrator.
 *
 * No policies are declared in `res/xml/device_admin.xml`, so this receiver can never
 * lock, wipe or reconfigure the device.
 */
class UninstallGuardReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        UninstallLock.onEnabled(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        UninstallLock.onDisabled(context)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        // Android shows this text in the deactivation dialog, so the warning is
        // delivered exactly where the user is about to remove the barrier.
        return context.getString(R.string.device_admin_disable_warning)
    }
}
