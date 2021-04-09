package asaa.bachelor.bleconnector.connections

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(private val btManager: BluetoothManager) :
    ViewModel() {
    val isScanning = MutableLiveData(false)
    val btDevicesSize = MutableLiveData(btManager.btDevices.size)

    fun startScanning(scanLowEnergy: Boolean = true) {
        if (scanLowEnergy) {
            Timber.i("onClick: StartDiscovery LE")
            isScanning.postValue(true)
            btManager.startLowEnergyDiscovery()
        } else {
            Timber.i("onClick: StartDiscovery Classic")
            isScanning.postValue(true)
            btManager.startClassicDiscovery()
        }
    }

    fun stopScanning() {
        Timber.i("onClick: StopDiscovery")
        isScanning.postValue(false)
        btManager.stopDiscovery()
        btDevicesSize.postValue(btManager.btDevices.size)
    }

    fun closeAll() {

    }

}