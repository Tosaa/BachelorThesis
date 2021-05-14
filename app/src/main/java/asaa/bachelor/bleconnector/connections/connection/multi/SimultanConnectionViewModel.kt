package asaa.bachelor.bleconnector.connections.connection.multi

import androidx.lifecycle.ViewModel
import asaa.bachelor.bleconnector.bt.custom.le.ESP32Device
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimultanConnectionViewModel @Inject constructor(val bluetoothManager: BluetoothManager) :
    ViewModel() {

    var firstConnection = ConnectionItem("24:0A:C4:60:E5:D2", bluetoothManager).asLiveData
    var secondConnection = ConnectionItem("24:0A:C4:60:EF:3A", bluetoothManager).asLiveData
    var thirdConnection = ConnectionItem("24:0A:C4:61:78:D2", bluetoothManager).asLiveData
    var connections = listOf(firstConnection.value, secondConnection.value, thirdConnection.value)

    fun updateConnections(){
        connections.forEach {
            it?.refresh()
        }
    }

    companion object{
        val DEVICE_A = "24:0A:C4:60:E5:D2"
            val DEVICE_B = "24:0A:C4:60:EF:3A"
                val DEVICE_C = "24:0A:C4:61:78:D2"
    }
}