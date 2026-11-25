package com.example.freevpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.StringReader

class MyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var backend: GoBackend? = null
    private var isTunnelUp = false

    companion object {
        const val ACTION_START = "com.example.freevpn.ACTION_START"
        const val ACTION_STOP = "com.example.freevpn.ACTION_STOP"

        // Jab aapke paas WireGuard config aaye to yahan paste karein:
        private const val WIREGUARD_CONFIG_TEXT = """
            [Interface]
            PrivateKey = REPLACE_WITH_CLIENT_PRIVATE_KEY
            Address = 10.66.66.2/32
            DNS = 1.1.1.1

            [Peer]
            PublicKey = REPLACE_WITH_SERVER_PUBLIC_KEY
            Endpoint = 127.0.0.1:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 25
        """.trimIndent()
    }

    private val wgTunnel = object : Tunnel {
        override fun getName(): String = "FreeVpnTunnel"
        override fun onStateChange(newState: Tunnel.State) {
            isTunnelUp = (newState == Tunnel.State.UP)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startWireGuardOrFallback()
            ACTION_STOP -> stopVpn()
        }
        return START_NOT_STICKY
    }

    private fun startWireGuardOrFallback() {
        try {
            if (backend == null) {
                backend = GoBackend(this)
            }

            // Agar real keys daali gayi hain to WireGuard backend chalayein
            if (!WIREGUARD_CONFIG_TEXT.contains("REPLACE_WITH_")) {
                val config = Config.parse(BufferedReader(StringReader(WIREGUARD_CONFIG_TEXT.trim())))
                backend?.setState(wgTunnel, Tunnel.State.UP, config)
                isTunnelUp = true
                return
            }

            // Jab tak placeholder keys hain, tab tak local VpnService interface open rakhein
            if (vpnInterface == null) {
                vpnInterface = Builder()
                    .setSession("Free VPN")
                    .addAddress("10.0.0.2", 32)
                    .establish()
                if (vpnInterface == null) {
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            closeVpn()
            stopSelf()
        }
    }

    private fun stopVpn() {
        closeVpn()
        stopSelf()
    }

    private fun closeVpn() {
        try {
            if (isTunnelUp) {
                backend?.setState(wgTunnel, Tunnel.State.DOWN, null)
                isTunnelUp = false
            }
        } catch (_: Exception) {
        }

        try {
            vpnInterface?.close()
        } catch (_: Exception) {
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
