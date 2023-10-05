package com.example.c20bt05

import com.example.c20bt05.MainActivity
import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class BluetoothDataActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_data)
        val textViewDadosBt = findViewById<TextView>(R.id.textViewDadosBt)

        // Obter o dado Bluetooth do `Intent`
        val bluetoothData = intent?.getStringExtra("bluetooth_data")

        // Exibir o dado Bluetooth na tela
        textViewDadosBt.text = bluetoothData
    }
}
