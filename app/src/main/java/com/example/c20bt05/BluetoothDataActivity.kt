package com.example.c20bt05

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class BluetoothDataActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_data)

        val textViewDadosBt = findViewById<TextView>(R.id.textViewDadosBt)

        // Recupere os dados Bluetooth da intent
        val dadosBluetooth = intent.getStringExtra("dados_bluetooth")

        // Atualize o TextView na BluetoothDataActivity com os dados recebidos
        textViewDadosBt.text = dadosBluetooth
    }
}
