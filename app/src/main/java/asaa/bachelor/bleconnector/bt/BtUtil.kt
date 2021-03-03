package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothDevice
import asaa.bachelor.bleconnector.connections.BluetoothDeviceAdapter

object BtUtil {

    fun resolveBond(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_BONDED -> "Bonded"
            BluetoothDevice.BOND_BONDING -> "Bonding"
            BluetoothDevice.BOND_NONE -> "Not Bond"
            else -> "unknown Bond state: $bondState"
        }
    }

    fun resolveDeviceType(deviceType: Int): String {
        return when (deviceType) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "CLASSIC"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "DUAL"
            BluetoothDevice.DEVICE_TYPE_LE -> "LE"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN DEVICE TYPE"
        }
    }
}