package com.aistudio.breakfree.recovr.monitoring

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.aistudio.breakfree.recovr.MainActivity
import com.aistudio.breakfree.recovr.R
import com.aistudio.breakfree.recovr.localization.Translations
import com.aistudio.breakfree.recovr.model.AppLanguage
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * The on-device half of the DNS shield: a VPN that carries **only DNS queries**.
 *
 * ## Why a VPN at all
 *
 * Android does not let an ordinary app write the system's Private DNS setting; that needs
 * the Device Owner role (see [PrivateDnsShield]). A VPN is the only mechanism a normal
 * install has for taking control of resolution, so this service exists as the fallback and
 * as the default for everyone who is not a Device Owner.
 *
 * ## How narrow it is
 *
 * The tunnel has a single /32 route per resolver address. Every other packet — web pages,
 * video, chat, updates — never enters it, so there is no traffic forwarding, no NAT, and no
 * throughput cost. Only lookups addressed to the resolver are read, matched against
 * [DnsBlocklist], and either answered locally or relayed.
 *
 * A VPN cannot be hidden from the user: Android shows the key indicator and this service
 * posts an ongoing notification. The user can also switch it off in system settings, which
 * is why [DnsShield] reports the live state instead of assuming it is still on.
 */
class DnsFilterVpnService : VpnService() {

    companion object {
        private const val TAG = "DnsFilterVpnService"

        private const val ACTION_START = "com.aistudio.breakfree.recovr.DNS_SHIELD_START"
        private const val ACTION_STOP = "com.aistudio.breakfree.recovr.DNS_SHIELD_STOP"
        private const val ACTION_REFRESH = "com.aistudio.breakfree.recovr.DNS_SHIELD_REFRESH"

        private const val CHANNEL_ID = "dns_shield"
        private const val NOTIFICATION_ID = 0x5D05

        /** Cloudflare for Families: blocks adult content and malware, and logs nothing. */
        private const val UPSTREAM_PRIMARY = "1.1.1.3"
        private const val UPSTREAM_SECONDARY = "1.0.0.3"

        /** The tunnel's own address; it never leaves the device. */
        private const val TUN_ADDRESS = "10.111.222.1"

        private const val MTU = 1500
        private const val UPSTREAM_TIMEOUT_MS = 5000
        private const val RCODE_SERVFAIL = 2

        @Volatile
        private var running = false

        /**
         * True while the tunnel is up. The VPN dies with the process, so this cannot go
         * stale across a restart the way a persisted flag would.
         */
        fun isRunning(): Boolean = running

        fun start(context: Context) {
            val intent = Intent(context, DnsFilterVpnService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DnsFilterVpnService::class.java))
        }

        /**
         * Re-posts the ongoing notification with the current wording. Used when Stealth
         * Mode is toggled while the tunnel is already up. Does nothing when the filter is
         * off, so it can never start a VPN by accident.
         */
        fun refreshNotification(context: Context) {
            if (!running) return
            try {
                context.startService(
                    Intent(context, DnsFilterVpnService::class.java).setAction(ACTION_REFRESH)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not refresh the DNS notification", e)
            }
        }
    }

    private var tunnel: ParcelFileDescriptor? = null
    private var reader: Thread? = null
    private val workers = Executors.newFixedThreadPool(4)
    private val writeLock = Any()

    @Volatile
    private var active = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_REFRESH) {
            if (running) {
                startInForeground()
                return START_STICKY
            }
            stopSelf()
            return START_NOT_STICKY
        }

        startInForeground()

        if (!active) {
            if (!establish()) {
                Log.w(TAG, "Could not establish the DNS tunnel")
                stopSelf()
                return START_NOT_STICKY
            }
            active = true
            running = true
            startReading()
        }
        return START_STICKY
    }

    /**
     * The tunnel carries DNS only: the resolver's addresses are routed into it and nothing
     * else is. Our own app is excluded so BreakFree's Firebase traffic can never loop back
     * into the filter.
     */
    private fun establish(): Boolean {
        val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(this) &&
            com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(this)
        val builder = Builder()
            // The VPN session name is visible in system VPN settings, so it must not say
            // "BreakFree" while Stealth Mode is on.
            .setSession(
                if (stealth) Translations.t("stealth_vpn_session", currentLanguage())
                else getString(R.string.app_name)
            )
            .setMtu(MTU)
            .addAddress(TUN_ADDRESS, 32)
            .addDnsServer(UPSTREAM_PRIMARY)
            .addDnsServer(UPSTREAM_SECONDARY)
            .addRoute(UPSTREAM_PRIMARY, 32)
            .addRoute(UPSTREAM_SECONDARY, 32)
            .setBlocking(true)

        try {
            builder.addDisallowedApplication(packageName)
        } catch (e: Exception) {
            // Not fatal: our upstream socket is protected below as well.
            Log.w(TAG, "Could not exclude our own package from the tunnel", e)
        }

        tunnel = try {
            builder.establish()
        } catch (e: Exception) {
            Log.w(TAG, "establish() failed", e)
            null
        }
        return tunnel != null
    }

    private fun startReading() {
        val descriptor = tunnel ?: return
        val input = FileInputStream(descriptor.fileDescriptor)
        val output = FileOutputStream(descriptor.fileDescriptor)
        val buffer = ByteArray(32767)

        reader = thread(name = "dns-shield-reader", isDaemon = true) {
            try {
                while (active) {
                    val read = input.read(buffer)
                    if (read <= 0) continue
                    val packet = buffer.copyOf(read)
                    workers.execute { handle(packet, read, output) }
                }
            } catch (e: Exception) {
                if (active) Log.w(TAG, "Tunnel reader stopped", e)
            }
        }
    }

    /**
     * One DNS query: blocked hosts are answered locally, everything else is relayed to the
     * family resolver. Runs on a worker thread; a slow lookup can never stall the reader.
     */
    private fun handle(packet: ByteArray, length: Int, output: FileOutputStream) {
        val query = try {
            DnsPacket.parseQuery(packet, length)
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse a DNS query", e)
            null
        } ?: return

        if (DnsBlocklist.isBlocked(query.host)) {
            write(output, DnsPacket.buildNxDomain(query, packet))
            return
        }

        val answer = resolve(query.payload(packet))
        val reply = if (answer != null) {
            DnsPacket.buildResponse(query, answer)
        } else {
            DnsPacket.buildError(query, packet, RCODE_SERVFAIL)
        }
        write(output, reply)
    }

    /** Sends the query to the family resolver, bypassing the tunnel. */
    private fun resolve(payload: ByteArray): ByteArray? {
        for (upstream in arrayOf(UPSTREAM_PRIMARY, UPSTREAM_SECONDARY)) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                // protect() keeps the lookup itself out of the tunnel, so it cannot loop.
                if (!protect(socket)) {
                    Log.w(TAG, "Could not protect the upstream socket")
                    socket.close()
                    return null
                }
                socket.soTimeout = UPSTREAM_TIMEOUT_MS

                val address = InetAddress.getByName(upstream)
                socket.send(DatagramPacket(payload, payload.size, address, 53))

                val responseBuffer = ByteArray(4096)
                val response = DatagramPacket(responseBuffer, responseBuffer.size)
                socket.receive(response)
                return response.data.copyOfRange(0, response.length)
            } catch (e: Exception) {
                Log.w(TAG, "Resolver $upstream did not answer", e)
            } finally {
                socket?.close()
            }
        }
        return null
    }

    private fun write(output: FileOutputStream, packet: ByteArray) {
        try {
            synchronized(writeLock) {
                output.write(packet)
            }
        } catch (e: Exception) {
            if (active) Log.w(TAG, "Could not write a DNS answer", e)
        }
    }

    override fun onRevoke() {
        // The user (or another VPN) took the tunnel away. Nothing to undo: just stop.
        Log.i(TAG, "The VPN was revoked by the system")
        stopSelf()
    }

    override fun onDestroy() {
        active = false
        running = false
        workers.shutdownNow()
        try {
            workers.awaitTermination(1, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        try {
            tunnel?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Could not close the tunnel", e)
        }
        tunnel = null
        reader = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startInForeground() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
    }

    private fun buildNotification(): android.app.Notification {
        val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(this) &&
            com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(this)
        val language = currentLanguage()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(if (stealth) R.drawable.ic_stat_neutral else R.drawable.ic_stat_shield)
        .setContentTitle(
            Translations.t(if (stealth) "stealth_service_title" else "dns_vpn_notification_title", language)
        )
        .setContentText(
            Translations.t(if (stealth) "stealth_service_text" else "dns_vpn_notification_text", language)
        )
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setShowWhen(false)
        .setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(openAppIntent())
        .addAction(
            0,
            Translations.t("dns_vpn_notification_stop", currentLanguage()),
            stopIntent()
        )
        .build()
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopIntent(): PendingIntent {
        val intent = Intent(this, DnsFilterVpnService::class.java).setAction(ACTION_STOP)
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val language = currentLanguage()
        val stealth = com.aistudio.breakfree.recovr.stealth.StealthPreferences.isStealthEnabled(this) &&
            com.aistudio.breakfree.recovr.stealth.StealthPreferences.discreetNotifications(this)
        val channel = NotificationChannel(
            CHANNEL_ID,
            Translations.t(
                if (stealth) "stealth_notif_channel" else "dns_vpn_notification_channel",
                language
            ),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = Translations.t(
                if (stealth) "stealth_notif_channel_desc" else "dns_vpn_notification_channel_desc",
                language
            )
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun currentLanguage(): AppLanguage {
        // Same store and key the repository writes when the user switches language.
        val code = getSharedPreferences(MonitoringPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            .getString("selected_language", "en")
        return if (code == "ar") AppLanguage.ARABIC else AppLanguage.ENGLISH
    }
}
