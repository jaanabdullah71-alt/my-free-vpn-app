package com.example.freevpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class MyVpnService : VpnService() {

private var vpnInterface: ParcelFileDescriptor? = null

companion object {
    const val ACTION_START = "com.example.freevpn.ACTION_START"
    const val ACTION_STOP = "com.example.freevpn.ACTION_STOP"
}

override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int
): Int {

    when (intent?.action) {

        ACTION_START -> {
            if (vpnInterface == null) {
                try {
                    vpnInterface = Builder()
                        .setSession("Free VPN")
                        .addAddress("10.0.0.2", 32)
                        .establish()

                    if (vpnInterface == null) {
                        stopSelf()
                    }

                } catch (e: Exception) {
                    closeVpn()
                    stopSelf()
                }
            }
        }

        ACTION_STOP -> {
            closeVpn()
            stopSelf()
        }
    }

    return START_NOT_STICKY
}

private fun closeVpn() {
    try {
        vpnInterface?.close()
    } catch (e: Exception) {
    }

    vpnInterface = null
}

override fun onRevoke() {
    closeVpn()
    stopSelf()
    super.onRevoke()
}

override fun onDestroy() {
    closeVpn()
    super.onDestroy()
}

}
