package asaa.bachelor.bleconnector.connections

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(private val btOrchestrator: BluetoothOrchestrator) :
    ViewModel() {
    val isScanning = MutableLiveData(false)
    val btDevicesSize = MutableLiveData(btOrchestrator.btDevices.size)

    fun startScanning(scanLowEnergy: Boolean = true) {
        if (scanLowEnergy) {
            Timber.i("onClick: StartDiscovery LE")
            isScanning.postValue(true)
            btOrchestrator.startDiscovery()
        } else {
            Timber.i("onClick: StartDiscovery LE")
            isScanning.postValue(true)
            btOrchestrator.startClassicDiscovery()
        }
    }

    fun stopScanning() {
        Timber.i("onClick: StopDiscovery")
        isScanning.postValue(false)
        btOrchestrator.stopDiscovery()
        btDevicesSize.postValue(btOrchestrator.btDevices.size)
    }

    fun closeAll() {

    }

}