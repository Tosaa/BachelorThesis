package asaa.bachelor.bleconnector.connections.connection.multi

import androidx.lifecycle.ViewModel
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SimultanConnectionViewModel @Inject constructor(val bluetoothManager: BluetoothManager) :
    ViewModel() {

    val firstConnection = ConnectionItem("24:0A:C4:60:E5:D2", bluetoothManager).asLiveData
    val secondConnection = ConnectionItem("24:0A:C4:60:EF:3A", bluetoothManager).asLiveData
    val thirdConnection = ConnectionItem("24:0A:C4:61:78:D2", bluetoothManager).asLiveData
    val connections = listOf(firstConnection.value, secondConnection.value, thirdConnection.value)

}