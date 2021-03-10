package asaa.bachelor.bleconnector.connections.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import asaa.bachelor.bleconnector.bt.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "ConnectionDetailViewModel"

@HiltViewModel
class ConnectionDetailViewModel @Inject constructor(val bluetoothOrchestrator: BluetoothOrchestrator) :
    ViewModel() {

    // Bluetooth Device
    val bluetoothDevice = MutableLiveData<BluetoothDevice>()
    val macAddress = bluetoothDevice.map { it.address }
    val deviceName = bluetoothDevice.map { it.name }

    val connectionState = MutableLiveData<ConnectionStatus>()
    val isConnected = connectionState.map { it == ConnectionStatus.CONNECTED }
    val connectButtonText = isConnected.map { if (it) "DISCONNECT" else "CONNECT" }

    // -- isConnected
    val bondState = MutableLiveData<BondState>(BondState.NOT_BOND)
    val isBond = bondState.map { it == BondState.BONDED }

    // -- canBond
    val discoverState = MutableLiveData<DiscoveryStatus>()
    val isDiscovered = discoverState.map { it is DiscoveryStatus.DISCOVERED }

    // Services
    val services = MutableLiveData<List<BluetoothGattService>>()

    val containsBatteryService = services.map {
        it.any {
            it.uuid == CommonServices.Battery.uuid
        }
    }
}