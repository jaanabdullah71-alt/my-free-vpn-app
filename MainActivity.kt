package com.example.freevpn

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var isConnected = false
    private var connectedSeconds = 0L
    private val vpnRequestCode = 1001

    private lateinit var tvStatus: TextView
    private lateinit var btnConnect: Button

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isConnected) {
                connectedSeconds++
                updateStatusWithTimer()
                timerHandler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnConnect = findViewById(R.id.btnConnect)

        syncStateWithSystem()

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
        syncStateWithSystem()
    }

    override fun onPause() {
        super.onPause()
        timerHandler.removeCallbacks(timerRunnable)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == vpnRequestCode) {
            if (resultCode == RESULT_OK) {
                startVpn()
            } else {
                isConnected = false
                stopTimer()
                updateUi()
            }
        }
    }

    private fun startVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = MyVpnService.ACTION_START
        startService(intent)

        isConnected = true
        connectedSeconds = 0L
        updateUi()
        startTimer()
    }

    private fun stopVpn() {
        val intent = Intent(this, MyVpnService::class.java)
        intent.action = MyVpnService.ACTION_STOP
        startService(intent)

        isConnected = false
        stopTimer()
        updateUi()
    }

    private fun isSystemVpnActive(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }

    private fun syncStateWithSystem() {
        val active = isSystemVpnActive()
        if (active && !isConnected) {
            isConnected = true
            connectedSeconds = 0L
        } else if (!active) {
            isConnected = false
        }

        updateUi()
        if (isConnected) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    private fun startTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        timerHandler.postDelayed(timerRunnable, 1000L)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        connectedSeconds = 0L
    }

    private fun updateStatusWithTimer() {
        val hours = connectedSeconds / 3600
        val minutes = (connectedSeconds % 3600) / 60
        val seconds = connectedSeconds % 60
        val formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        tvStatus.text = "Connected • $formattedTime"
    }

    private fun updateUi() {
        if (isConnected) {
            updateStatusWithTimer()
            btnConnect.text = "DISCONNECT"
        } else {
            tvStatus.text = "Disconnected"
            btnConnect.text = "CONNECT"
        }
    }
}
