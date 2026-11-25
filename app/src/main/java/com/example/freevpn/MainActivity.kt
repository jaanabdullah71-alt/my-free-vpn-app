package com.example.freevpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isConnected = false
    private val vpnRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        btnConnect.setOnClickListener {
            if (!isConnected) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, vpnRequestCode)
                } else {
                    startVpnService(tvStatus, btnConnect)
                }
            } else {
                stopVpnService(tvStatus, btnConnect)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == vpnRequestCode && resultCode == RESULT_OK) {
            val tvStatus = findViewById<TextView>(R.id.tvStatus)
            val btnConnect = findViewById<Button>(R.id.btnConnect)
            startVpnService(tvStatus, btnConnect)
        }
    }

    private fun startVpnService(tvStatus: TextView, btnConnect: Button) {
        val intent = Intent(this, MyVpnService::class.java)
        startService(intent)
        isConnected = true
        tvStatus.text = "Connected"
        btnConnect.text = "Disconnect"
    }

    private fun stopVpnService(tvStatus: TextView, btnConnect: Button) {
        val intent = Intent(this, MyVpnService::class.java).apply {
            action = "ACTION_STOP"
        }
        startService(intent)
        isConnected = false
        tvStatus.text = "Disconnected"
        btnConnect.text = "Connect"
    }
}
