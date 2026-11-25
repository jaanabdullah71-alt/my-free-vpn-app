package com.example.freevpn

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        btnConnect.setOnClickListener {
            isConnected = !isConnected
            if (isConnected) {
                tvStatus.text = "Connected"
                btnConnect.text = "Disconnect"
            } else {
                tvStatus.text = "Disconnected"
                btnConnect.text = "Connect"
            }
        }
    }
}
