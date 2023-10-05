package com.example.c20bt05

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.c20bt05.BluetoothService.LocalBinder
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.bluetooth.BluetoothSocket

@SuppressLint("MissingPermission")
class MainActivity : Activity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices: MutableList<BluetoothDevice> = ArrayList()
    private lateinit var deviceListView: ListView
    private lateinit var buttonPagina2: Button
    private lateinit var textViewReceivedData: TextView

    private var bluetoothData: String = ""

    private var bluetoothService: BluetoothService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService = null
            isBound = false
        }
    }

    companion object {
        private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceListView = findViewById(R.id.deviceListView)
        textViewReceivedData = findViewById(R.id.textViewReceivedData)

        val buttonPagina2 = findViewById<Button>(R.id.buttonPagina2)
        buttonPagina2.setOnClickListener {
            val intent = Intent(this@MainActivity, BluetoothDataActivity::class.java)
            intent.putExtra("dados_bluetooth", bluetoothData)
            startActivity(intent)
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = discoveredDevices[position]
            ConnectThread(selectedDevice).start()
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        } else {
            val pairedDevices = bluetoothAdapter.bondedDevices
            discoveredDevices.addAll(pairedDevices)

            val deviceListAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, discoveredDevices.map { it.name })
            deviceListView.adapter = deviceListAdapter
        }

        // Solicitar permissão BLUETOOTH_SCAN se ainda não estiver concedida
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_BLUETOOTH_SCAN_PERMISSION
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind ao serviço BluetoothService
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        // Desvincular o serviço BluetoothService
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val uuid = device.uuids[0].uuid
        private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(uuid)
        }

        override fun run() {
            bluetoothAdapter.cancelDiscovery()

            try {
                socket?.use { socket ->
                    socket.connect()
                    textViewReceivedData.text = "Connected to ${device.name}"

                    // Iniciar a leitura dos dados da balança no serviço
                    // Na sua atividade MainActivity, chame startDataReading sem passar um socket
                    bluetoothService?.startDataReading()
                }
            } catch (e: Exception) {
                textViewReceivedData.text = "Failed to connect to ${device.name}"
            }
        }
    }
}
