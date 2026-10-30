package com.aistudio.breakfree.recovr.monitoring

import android.app.Activity
import android.os.Bundle

/**
 * Entry point the system's setup wizard launches when it provisions BreakFree as
 * Device Owner (the QR / NFC flow). The platform performs the actual provisioning;
 * a DPC only has to accept it and finish, which is what this does.
 *
 * It is exported because the setup wizard lives in another process, but it starts no
 * work and exposes nothing: an ordinary app that launches it gets an empty activity
 * that closes immediately.
 */
class DeviceOwnerProvisioningActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK)
        finish()
    }
}
