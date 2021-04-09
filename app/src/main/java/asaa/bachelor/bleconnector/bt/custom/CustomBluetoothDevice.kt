package asaa.bachelor.bleconnector.bt.custom

import android.bluetooth.BluetoothDevice

open class CustomBluetoothDevice(val device: BluetoothDevice) {
    val deviceTag = device.address.toString()
}