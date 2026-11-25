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
            ACTION_STOP -> {
                stopVpnTunnel()
                START_NOT_STICKY
            }
            else -> {
                startVpnTunnel(loadSampleConfig())
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

    private fun closeVpnInterface() {
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        } finally {
            vpnInterface = null
        }
    }

    private fun stopVpnTunnel() {
        closeVpnInterface()
        stopSelf()
    }

    override fun onRevoke() {
        closeVpnInterface()
        stopSelf()
        super.onRevoke()
    }

    override fun onDestroy() {
        closeVpnInterface()
        super.onDestroy()
    }
}
