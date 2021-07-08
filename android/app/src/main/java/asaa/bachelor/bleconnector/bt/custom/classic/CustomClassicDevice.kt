package asaa.bachelor.bleconnector.bt.custom.classic

import android.bluetooth.BluetoothDevice
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import asaa.bachelor.bleconnector.bt.ConnectionStatus.DISCONNECTED
import java.util.*

class CustomClassicDevice(
    device: BluetoothDevice
) : BluetoothClassicDevice(device) {
    override var connectionStatus: ConnectionStatus = DISCONNECTED("")
    override val name = "BluetoothConnectionTest"
    override val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

}