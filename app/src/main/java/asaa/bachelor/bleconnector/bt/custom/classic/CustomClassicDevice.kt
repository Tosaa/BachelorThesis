package asaa.bachelor.bleconnector.bt.custom.classic

import android.bluetooth.BluetoothDevice
import java.util.*

class CustomClassicDevice(
    device: BluetoothDevice,
) : BluetoothClassicDevice(device) {

    override val name = "BluetoothConnectionTest"
    override val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

}