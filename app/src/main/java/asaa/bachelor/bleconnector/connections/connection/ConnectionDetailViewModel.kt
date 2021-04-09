package asaa.bachelor.bleconnector.connections.connection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CustomService
import asaa.bachelor.bleconnector.bt.custom.le.ESP32Device
import asaa.bachelor.bleconnector.bt.custom.le.ESP32DeviceObserver
import asaa.bachelor.bleconnector.bt.custom.le.NotificationStatus
import asaa.bachelor.bleconnector.bt.custom.le.WriteStatus
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "ConnectionDetailViewModel"

@HiltViewModel
class ConnectionDetailViewModel @Inject constructor(val bluetoothManager: BluetoothManager) :
    ViewModel(), ESP32DeviceObserver, DeviceStateObserver {

    // Bluetooth Device
    val bluetoothDevice = MutableLiveData<ESP32Device>()
    val macAddress = bluetoothDevice.map { it.device.address }
    val deviceName = bluetoothDevice.map { it.device.name }

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
    // Custom Service

    val customService = services.distinctUntilChanged().map { it.find { CustomService.mapIfExists(it.uuid.toString()) == CustomService.CUSTOM_SERVICE_1 } }
    val containsCustomService = customService.distinctUntilChanged().map { it != null }
    val customReadValue = MutableLiveData<String>("None")
    val customNotifyValue = MutableLiveData<String>("None")
    val isNotifyActive = MutableLiveData<Boolean>(false)
    val customIndicateValue = MutableLiveData<String>("None")
    val isIndicateActive = MutableLiveData<Boolean>(false)

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        connectionState.postValue(newStatus)
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        discoverState.postValue(newDiscoveryState)
    }

    override fun onBondStateChanged(bond: BondState) {
        bondState.postValue(bond)
    }

    override fun onCharacteristic1Changed(newValue: String) {
        customReadValue.postValue(newValue)
    }

    override fun onCharacteristic2Changed(newValue: String) {
        customReadValue.postValue(newValue)
    }

    override fun writeCommandStatusChanged(writeStatus: WriteStatus) {
        if (writeStatus is WriteStatus.DONE) {
            customReadValue.postValue(writeStatus.writtenValue)
        }
    }

    override fun notifyStatusChanged(notificationStatus: NotificationStatus) {
        Timber.i("notify status changed: $notificationStatus")

        isNotifyActive.postValue(notificationStatus is NotificationStatus.DONE && notificationStatus.isActive)
    }

    override fun notifyValueChanged(newValue: String) {
        customNotifyValue.postValue(newValue)
    }

    override fun indicateStatusChanged(notificationStatus: NotificationStatus) {
        Timber.i("indicate status changed: $notificationStatus")
        isIndicateActive.postValue(notificationStatus is NotificationStatus.DONE && notificationStatus.isActive)
    }

    override fun indicateValueChanged(newValue: String) {
        customIndicateValue.postValue(newValue)
    }

    override fun connectionPropertyChanged() {
        TODO("Not yet implemented")
    }
}