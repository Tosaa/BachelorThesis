package asaa.bachelor.bleconnector.connections.connection.classic

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CommonServices
import asaa.bachelor.bleconnector.bt.common.CustomService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "ConnectionDetailViewModel"

@HiltViewModel
class ConnectionDetailClassicViewModel @Inject constructor(val bluetoothOrchestrator: BluetoothOrchestrator) :
    ViewModel() {

    // Bluetooth Device
    val bluetoothDevice = MutableLiveData<BluetoothDevice>()
    val macAddress = bluetoothDevice.map { it.address }
    val deviceName = bluetoothDevice.map { it.name }

    val connectionState = MutableLiveData<ConnectionStatus>()
    val isConnected = connectionState.map { it == ConnectionStatus.CONNECTED }
    val connectButtonText = "Connect"
}