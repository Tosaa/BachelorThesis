package asaa.bachelor.bleconnector.connections.connection

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import asaa.bachelor.bleconnector.bt.BluetoothConnection
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "ConnectionDetailViewModel"

@HiltViewModel
class ConnectionDetailViewModel @Inject constructor(val bluetoothOrchestrator: BluetoothOrchestrator) :
    ViewModel() {
    val macAddress = MutableLiveData<String>("")
    val connectionState = MutableLiveData<String>("")
    val isConnected = connectionState.map { it == ConnectionStatus.CONNECTED.toString() }
    val services = MutableLiveData<List<BluetoothGattService>>()
    val serviceUUIDs = services.map { it.joinToString(separator = "\n") { it.uuid.toString() } }

    fun reconnect() {
        Log.v(TAG, "reconnect mac Addr: ${macAddress.value}")
        macAddress.value?.takeIf { !it.isEmpty() }?.let {
            bluetoothOrchestrator.connect(it)
        }
    }

    fun disconnect() {
        macAddress.value?.takeIf { !it.isEmpty() }?.let {
            bluetoothOrchestrator.disconnect(it)
        }
    }
}