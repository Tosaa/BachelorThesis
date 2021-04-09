package asaa.bachelor.bleconnector.bt.custom.classic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import asaa.bachelor.bleconnector.bt.custom.CustomBluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

abstract class BluetoothClassicDevice(device: BluetoothDevice) : CustomBluetoothDevice(device) {

    abstract val uuid: UUID
    abstract val name: String

    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    var blSocket: BluetoothSocket? = null
    private var connectionState: ConnectionStatus = ConnectionStatus.DISCONNECTED("")
        set(value) {
            if (field != value) {
                Timber.i("$deviceTag Connection Status changed $field -> $value")
                field = value
                notifyConnectionStateChanged(field)
            }
        }

    // observer
    val observers: MutableList<ConnectionStateObserver> = mutableListOf()

    fun addObserver(o: ConnectionStateObserver) {
        Timber.d("$deviceTag: add Observer: $o")
        observers.add(o)
        o.onConnectionStateChanged(connectionState)
    }

    fun removeObserver(o: ConnectionStateObserver) {
        Timber.d("$deviceTag: remove Observer: $o")
        observers.remove(o)
    }

    private fun notifyConnectionStateChanged(status: ConnectionStatus) {
        observers.forEach {
            it.onConnectionStateChanged(status)
        }
    }

    private fun notifyOnWrite(byteArray: ByteArray) {
        observers.forEach {
            it.onWrite(byteArray)
        }
    }

    private fun notifyOnRead(bytes: ByteArray) {
        observers.forEach {
            it.onRead(bytes)
        }
    }

    // Connect
    // Disconnect
    // listener -> on Connection Changed
    fun disconnect(){
        btAdapter.cancelDiscovery()
        connectionState = ConnectionStatus.DISCONNECTED("End By User")
    }

    fun connect(coroutineScope: CoroutineScope) {
        btAdapter.cancelDiscovery()
        if(connectionState == ConnectionStatus.CONNECTING){
            connectionState = ConnectionStatus.DISCONNECTED("End by connect while being connected")
        }
        coroutineScope.launch(Dispatchers.IO) {
            connectionState = ConnectionStatus.CONNECTING
            Timber.d("$deviceTag Try to create InsecureRfcommSocketToServiceRecord")
            connectionState = try {

                blSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                blSocket?.let {
                    it.connect()
                }
                ConnectionStatus.CONNECTED
            } catch (e: Exception) {
                Timber.w("$deviceTag exception while connecting: $e")
                ConnectionStatus.DISCONNECTED(e.toString())
            }



            while (connectionState == ConnectionStatus.CONNECTED) {
                val text = CharArray(255)
                val reader = BufferedReader(InputStreamReader(blSocket?.inputStream))
                try {
                    val received = reader.readLine()
                    if (received.isNotEmpty()) {
                        notifyOnRead(received.map { it.toByte() }.toByteArray())
                    }
                } catch (e: Exception) {
                    Timber.w("$deviceTag exception while connected: $e")
                    connectionState = ConnectionStatus.DISCONNECTED(e.toString())
                }
            }
        }
    }

    // Todo: remove
    fun read(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            if (blSocket != null)
                Timber.v(blSocket?.inputStream?.readBytes().toString())
            else
                Timber.w("$deviceTag write: Bluetooth Socket was null")
        }
    }

    fun write(coroutineScope: CoroutineScope, string: String) {
        coroutineScope.launch(Dispatchers.IO) {
            if (blSocket != null) {
                blSocket?.outputStream?.write(string.toByteArray())
                notifyOnWrite(string.toByteArray())
            } else
                Timber.w("$deviceTag write: Bluetooth Socket was null")
        }
    }

    interface ConnectionStateObserver {
        fun onConnectionStateChanged(connectionStatus: ConnectionStatus)
        fun onWrite(bytes: ByteArray)
        fun onRead(bytes: ByteArray)
    }
}