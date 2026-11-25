package com.example.freevpn

import android.content.Intent
import android.net.VpnService

class MyVpnService : VpnService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_STOP" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // WireGuard tunnel setup will connect here
                return START_STICKY
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
