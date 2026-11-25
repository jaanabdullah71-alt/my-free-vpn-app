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

    private lateinit var tvStatus: TextView
    private lateinit var btnConnect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnConnect = findViewById(R.id.btnConnect)

        updateUi()

        btnConnect.setOnClickListener {
            if (!isConnected) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, vpnRequestCode)
                } else {
                    startVpn()
                }
            } else {
                stopVpn()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == vpnRequestCode) {
            if (resultCode == RESULT_OK) {
                startVpn()
            } else {
                isConnected = false
                updateUi()
            }
        }
    }

    private fun startVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = MyVpnService.ACTION_START
        startService(intent)

        isConnected = true
        updateUi()
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = MyVpnService.ACTION_STOP
        startService(intent)

        isConnected = false
        updateUi()
    }

    private fun updateUi() {
        if (isConnected) {
            tvStatus.text = "Connected"
            btnConnect.text = "DISCONNECT"
        } else {
            tvStatus.text = "Disconnected"
            btnConnect.text = "CONNECT"
        }
    }
}
