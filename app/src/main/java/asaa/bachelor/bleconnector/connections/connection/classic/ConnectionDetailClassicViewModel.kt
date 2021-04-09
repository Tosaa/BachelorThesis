package asaa.bachelor.bleconnector.connections.connection.classic

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import asaa.bachelor.bleconnector.bt.custom.classic.BluetoothClassicDevice
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "ConnectionDetailViewModel"

@HiltViewModel
class ConnectionDetailClassicViewModel @Inject constructor(val bluetoothManager: BluetoothManager) :
    ViewModel(), BluetoothClassicDevice.ConnectionStateObserver {

    // Bluetooth Device
    val bluetoothDevice = MutableLiveData<BluetoothDevice>()
    val macAddress = bluetoothDevice.map { it.address }
    val deviceName = bluetoothDevice.map { it.name }

    val connectionState = MutableLiveData<BluetoothClassicDevice.ConnectionState>()
    val isConnected = connectionState.map { it == BluetoothClassicDevice.ConnectionState.CONNECTED }
    val connectButtonText = "Connect"
    val latestRead = MutableLiveData("nothing read yet")
    override fun onConnectionStateChanged(connectionStatus: BluetoothClassicDevice.ConnectionState) {
        connectionState.postValue(connectionStatus)
    }

    override fun onWrite(bytes: ByteArray) {

    }

    override fun onRead(bytes: ByteArray) {
        bytes.joinToString { it.toChar().toString() }.let { latestRead.postValue(it) }
    }
}