package com.example.freevpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class MyVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    data class WireGuardConfig(
        val privateKey: String,
        val clientAddress: String,
        val dnsServer: String,
        val serverPublicKey: String,
        val endpoint: String,
        val allowedIps: String
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            "ACTION_STOP" -> {
                stopVpnTunnel()
                START_NOT_STICKY
            }
            else -> {
                val sampleConfig = loadSampleConfig()
                startVpnTunnel(sampleConfig)
                START_STICKY
            }
        }
    }

    private fun loadSampleConfig(): WireGuardConfig {
        return WireGuardConfig(
            privateKey = "<client_private_key>",
            clientAddress = "10.66.0.2",
            dnsServer = "1.1.1.1",
            serverPublicKey = "<server_public_key>",
            endpoint = "<server_ip>:<port>",
            allowedIps = "0.0.0.0/0"
        )
    }

    private fun startVpnTunnel(config: WireGuardConfig) {
        if (vpnInterface != null) return

        val builder = Builder()
            .setSession("Free VPN WireGuard")
            .addAddress(config.clientAddress, 32)
            .addDnsServer(config.dnsServer)
            .addRoute("0.0.0.0", 0)

        vpnInterface = builder.establish()
    }

    private fun stopVpnTunnel() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpnTunnel()
        super.onDestroy()
    }
}
