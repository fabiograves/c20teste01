package com.example.c20bt05

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothService : Service() {

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val discoveredDevices: MutableList<BluetoothDevice> = ArrayList()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothData: String = ""

    // Binder para interagir com o serviço
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // Listar os dispositivos Bluetooth emparelhados
        discoveredDevices.addAll(bluetoothAdapter.bondedDevices)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ...

        // Iniciar a conexão Bluetooth
        val selectedDevice: BluetoothDevice? = intent?.getParcelableExtra("device_address")
        val connectThread = ConnectThread(selectedDevice ?: return Service.START_NOT_STICKY)
        connectThread.start()

        return Service.START_NOT_STICKY
    }

    private fun readDataFromScale(): String {
        // Ler os dados Bluetooth do dispositivo conectado
        val inputStream = bluetoothSocket?.inputStream
        val buffer = ByteArray(1024)
        val bytesRead = inputStream?.read(buffer) ?: 0
        val data = String(buffer, 0, bytesRead).trim()

        return data
    }

    override fun onDestroy() {
        super.onDestroy()

        // Fechar a conexão Bluetooth
        bluetoothSocket?.close()
    }

    // Thread para ler os dados Bluetooth continuamente
    private inner class ReadDataThread : Thread() {
        override fun run() {
            while (true) {
                if (bluetoothSocket != null) {
                    val bluetoothData = readDataFromScale()
                    sendBroadcast(Intent("BluetoothData").putExtra("bluetooth_data", bluetoothData))
                }

                // Aguarde um período antes de ler novamente (por exemplo, 100 ms)
                Thread.sleep(100)
            }
        }
    }

    // Método para iniciar a leitura dos dados
    fun startDataReading() {
        // Inicie a thread de leitura contínua dos dados
        val readDataThread = ReadDataThread()
        readDataThread.start()
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
                    bluetoothSocket = socket

                    // Inicie a leitura dos dados
                    startDataReading()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
