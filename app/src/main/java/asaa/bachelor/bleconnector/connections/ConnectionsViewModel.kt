package asaa.bachelor.bleconnector.connections

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionsViewModel @Inject constructor(val btOrchestrator: BluetoothOrchestrator) :
    ViewModel() {
    val isScanning = MutableLiveData(false)
    val btDevicesSize = MutableLiveData(btOrchestrator.btDevices.size)

    fun startScanning() {
        isScanning.postValue(true)
        btOrchestrator.startDiscovery()
    }

    fun stopScanning() {
        isScanning.postValue(false)
        btOrchestrator.stopDiscovery()
        btDevicesSize.postValue(btOrchestrator.btDevices.size)
    }

    fun closeAll() {

    }

}