package asaa.bachelor.bleconnector.connections.connection

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
    val services = discoverState.map {
        if (it is DiscoveryStatus.DISCOVERED) it.services else emptyList()
    }

    // Battery
    val batteryService = services.distinctUntilChanged().map { it.find { CommonServices.mapIfExists(it.uuid.toString()) == CommonServices.Battery } }
    val containsBatteryService = batteryService.distinctUntilChanged().map {
        it != null
    }
    val batteryValue = MutableLiveData<String>("None")

    // Custom Service

    val customService = services.distinctUntilChanged().map { it.find { CustomService.mapIfExists(it.uuid.toString()) == CustomService.CUSTOM_SERVICE_1 } }
    val containsCustomService = customService.distinctUntilChanged().map { it != null }
    val customReadValue = MutableLiveData<String>("None")
    val customNotifyValue = MutableLiveData<String>("None")
    val isNotifyActive = MutableLiveData<Boolean>(false)
    val customIndicateValue = MutableLiveData<String>("None")
    val isIndicateActive = MutableLiveData<Boolean>(false)
}