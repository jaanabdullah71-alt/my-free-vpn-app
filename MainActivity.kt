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
import androidx.cardview.widget.CardView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var isConnected = false
    private var connectedSeconds = 0L
    private var serverIndex = 0
    private val vpnRequestCode = 1001

    private val servers = listOf(
        "Singapore — Free #1",
        "Germany — Free #2",
        "Netherlands — Free #3"
    )

    private lateinit var tvStatus: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvServerName: TextView
    private lateinit var btnConnect: Button
    private lateinit var cardServer: CardView

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isConnected) {
                connectedSeconds++
                updateTimerText()
                timerHandler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvServerName = findViewById(R.id.tvServerName)
        btnConnect = findViewById(R.id.btnConnect)
        cardServer = findViewById(R.id.cardServer)

        tvServerName.text = servers[serverIndex]
        syncStateWithSystem()

        cardServer.setOnClickListener {
            serverIndex = (serverIndex + 1) % servers.size
            tvServerName.text = servers[serverIndex]
        }

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
            connectedSeconds = 0L
        }

        updateUi()
        if (isConnected) {
            startTimer()
        } else {
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun startTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        updateTimerText()
        timerHandler.postDelayed(timerRunnable, 1000L)
    }

    private fun stopTimer() {
        timerHandler.removeCallbacks(timerRunnable)
        connectedSeconds = 0L
    }

    private fun updateTimerText() {
        val hours = connectedSeconds / 3600
        val minutes = (connectedSeconds % 3600) / 60
        val seconds = connectedSeconds % 60
        val formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        tvSubtitle.text = "Duration: $formattedTime"
    }

    private fun updateUi() {
        if (isConnected) {
            tvStatus.text = "Connected"
            updateTimerText()
            btnConnect.text = "DISCONNECT"
        } else {
            tvStatus.text = "Disconnected"
            tvSubtitle.text = "Tap connect to start VPN session"
            btnConnect.text = "CONNECT"
        }
    }
}
