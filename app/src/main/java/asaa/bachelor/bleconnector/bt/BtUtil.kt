package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import java.util.*

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

    enum class BluetoothGattStatus(val gattStatus: Int){
        GATT(BluetoothGatt.GATT),
        GATT_CONNECTION_CONGESTED(BluetoothGatt.GATT_CONNECTION_CONGESTED),
        GATT_FAILURE(BluetoothGatt.GATT_FAILURE),
        GATT_INSUFFICIENT_AUTHENTICATION(BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION),
        GATT_INSUFFICIENT_ENCRYPTION(BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION),
        GATT_INVALID_ATTRIBUTE_LENGTH(BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH),
        GATT_INVALID_OFFSET(BluetoothGatt.GATT_INVALID_OFFSET),
        GATT_READ_NOT_PERMITTED(BluetoothGatt.GATT_READ_NOT_PERMITTED),
        GATT_WRITE_NOT_PERMITTED(BluetoothGatt.GATT_WRITE_NOT_PERMITTED),
        GATT_REQUEST_NOT_SUPPORTED(BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED),
        GATT_SERVER(BluetoothGatt.GATT_SERVER),
        GATT_SUCCESS(BluetoothGatt.GATT_SUCCESS);

        companion object {
            private val map = BluetoothGattStatus.values().associateBy(BluetoothGattStatus::gattStatus)
            fun get(gattStatus: Int) = map[gattStatus]
        }
    }


    enum class BluetoothCharacteristicProperty(val bluetoothGattCharacteristicProperty: Int) {
        PROPERTY_BROADCAST(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
        PROPERTY_EXTENDED_PROPS(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS),
        PROPERTY_INDICATE(BluetoothGattCharacteristic.PROPERTY_INDICATE),
        PROPERTY_NOTIFY(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
        PROPERTY_READ(BluetoothGattCharacteristic.PROPERTY_READ),
        PROPERTY_SIGNED_WRITE(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
        PROPERTY_WRITE(BluetoothGattCharacteristic.PROPERTY_WRITE),
        PROPERTY_WRITE_NO_RESPONSE(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);

        companion object {
            private val map = values().associateBy(BluetoothCharacteristicProperty::bluetoothGattCharacteristicProperty)
            fun transform(property: Int): List<BluetoothCharacteristicProperty> {
                return map.values.filter { it.bluetoothGattCharacteristicProperty and property != 0 }
            }
        }
    }

    enum class CommonServices(val uuid: String) {
        BatteryService("0000180f-0000-1000-8000-00805f9b34fb");
        val asUUID = UUID.fromString(uuid)
    }

    enum class CommonCharacteristics(val uuid: String) {
        BatteryCharacteristic("00002a19-0000-1000-8000-00805f9b34fb");
        val asUUID = UUID.fromString(uuid)
    }
}