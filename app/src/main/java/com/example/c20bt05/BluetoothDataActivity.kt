package com.example.c20bt05

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class BluetoothDataActivity : AppCompatActivity() {

    private val textViewDadosBt by lazy { findViewById<TextView>(R.id.textViewDadosBt) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_data)

        // Obter o dado Bluetooth do `Intent`
        val bluetoothData = intent?.getStringExtra("bluetooth_data")

        // Exibir o dado Bluetooth na tela
        textViewDadosBt.text = bluetoothData
    }

    override fun onResume() {
        super.onResume()

        // Registrar um `BroadcastReceiver` para receber o `Intent` com os dados Bluetooth
        val filter = IntentFilter("BluetoothData")
        registerReceiver(broadcastReceiver, filter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Obter o dado Bluetooth do `Intent`
            val bluetoothData = intent.getStringExtra("bluetooth_data")

            // Exibir o dado Bluetooth na tela
            textViewDadosBt.text = bluetoothData
        }
    }
}
