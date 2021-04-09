package asaa.bachelor.bleconnector.bt.custom.classic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.InputStreamReader

class BluetoothClassicDevice(private val device: BluetoothDevice) {

    val UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val name = "BluetoothConnectionTest"

    val btAdapter = BluetoothAdapter.getDefaultAdapter()
    var blSocket: BluetoothSocket? = null
    private var connectionState = ConnectionState.DISCONNECTED
        set(value) {
            if (field != value) {
                Timber.i("Connection Status of ${device.address} changed $field -> $value")
                field = value
                notifyConnectionStateChanged(field)
            }
        }

    // ConnectionState
    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED;
    }

    // observer
    val observers: MutableList<ConnectionStateObserver> = mutableListOf()

    fun addObserver(o: ConnectionStateObserver) {
        Timber.d("${device.address}: add Observer: $o")
        observers.add(o)
        o.onConnectionStateChanged(connectionState)
    }

    fun removeObserver(o: ConnectionStateObserver) {
        Timber.d("${device.address}: remove Observer: $o")
        observers.remove(o)
    }

    private fun notifyConnectionStateChanged(status: ConnectionState) {
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

    fun connect(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            connectionState = ConnectionState.CONNECTING
            btAdapter.cancelDiscovery()
            Timber.d("Try to create InsecureRfcommSocketToServiceRecord")
            connectionState = try {

                blSocket = device.createInsecureRfcommSocketToServiceRecord(UUID)
                blSocket?.let {
                    it.connect()
                }
                ConnectionState.CONNECTED
            } catch (e: Exception) {
                Timber.w("exception while connecting: $e")
                ConnectionState.DISCONNECTED
            }



            while (connectionState == ConnectionState.CONNECTED) {
                val text = CharArray(10)
                try {
                    InputStreamReader(blSocket?.inputStream).read(text, 0, 10)
                    if (text.isNotEmpty()) {
                        notifyOnRead(text.map { it.toByte() }.toByteArray())
                    }
                } catch (e: Exception) {
                    Timber.w("exception while connected: $e")
                    connectionState = ConnectionState.DISCONNECTED
                }
            }
        }
    }

    fun read(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            if (blSocket != null)
                Timber.v(blSocket?.inputStream?.readBytes().toString())
            else
                Timber.w("write: Bluetooth Socket was null")
        }
    }

    fun write(coroutineScope: CoroutineScope, string: String) {
        coroutineScope.launch(Dispatchers.IO) {
            if (blSocket != null) {
                blSocket?.outputStream?.write(string.toByteArray())
                notifyOnWrite(string.toByteArray())
            } else
                Timber.w("write: Bluetooth Socket was null")
        }
    }

    interface ConnectionStateObserver {
        fun onConnectionStateChanged(connectionStatus: ConnectionState)
        fun onWrite(bytes: ByteArray)
        fun onRead(bytes: ByteArray)
    }
}