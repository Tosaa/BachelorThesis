package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile

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

    fun resolveBluetoothProfileToString(profilState: Int): String {
        return when (profilState) {
            BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
            BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
            BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
            BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
            else -> "UNKNOWN"

        }
    }

    fun resolveBluetoothProfileToConnectionStatus(profilState: Int, err: String = ""): ConnectionStatus {
        return when (profilState) {
            BluetoothProfile.STATE_CONNECTED -> ConnectionStatus.CONNECTED
            BluetoothProfile.STATE_CONNECTING -> ConnectionStatus.CONNECTING
            BluetoothProfile.STATE_DISCONNECTED -> ConnectionStatus.DISCONNECTED
            BluetoothProfile.STATE_DISCONNECTING -> ConnectionStatus.DISCONNECTING
            else -> ConnectionStatus.CONNECTING_FAILED("$profilState, $err")

        }
    }

    fun resolveBluetoothGattStatus(gattStatus: Int): String {
        return when (gattStatus) {
            BluetoothGatt.GATT -> "GATT"
            BluetoothGatt.GATT_CONNECTION_CONGESTED -> "CONNECTION_CONGESTED"
            BluetoothGatt.GATT_FAILURE -> "FAILURE"
            BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> "INSUFFICIENT_AUTHENTICATION"
            BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> "INSUFFICIENT_ENCRYPTION"
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> "INVALID_ATTRIBUTE_LENGTH"
            BluetoothGatt.GATT_INVALID_OFFSET -> "INVALID_OFFSET"
            BluetoothGatt.GATT_READ_NOT_PERMITTED -> "READ_NOT_PERMITTED"
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "WRITE_NOT_PERMITTED"
            BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> "REQUEST_NOT_SUPPORTED"
            BluetoothGatt.GATT_SERVER -> "SERVER"
            BluetoothGatt.GATT_SUCCESS -> "SUCCESS"
            else -> "UKNOWN"


        }
    }
}