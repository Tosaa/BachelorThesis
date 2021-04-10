package asaa.bachelor.bleconnector.bt.custom

import android.bluetooth.BluetoothDevice
import asaa.bachelor.bleconnector.bt.ConnectionStatus

abstract class CustomBluetoothDevice(val device: BluetoothDevice) {
    val deviceTag = device.address.toString()

    abstract var connectionStatus: ConnectionStatus
}