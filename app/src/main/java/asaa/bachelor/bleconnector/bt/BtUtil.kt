package asaa.bachelor.bleconnector.bt

import android.bluetooth.*
import asaa.bachelor.bleconnector.bt.common.CommonCharacteristics
import asaa.bachelor.bleconnector.bt.common.CommonServices
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.bt.common.CustomService
import timber.log.Timber
import java.util.*

object BtUtil {
    fun serviceToString(uuid: String): String {
        return CommonServices.mapIfExists(uuid)?.toString() ?: CustomService.mapIfExists(uuid)?.toString() ?: "unknown Service"
    }

    fun characteristicToString(uuid: String): String {
        return CommonCharacteristics.mapIfExists(uuid)?.toString() ?: CustomCharacteristic.mapIfExists(uuid)?.toString() ?: "unknown Characteristic"
    }

    fun gattCharacteristicToCharacteristic(gattCharacteristic: BluetoothGattCharacteristic): Any? {
        val uuid = gattCharacteristic.uuid.toString()
        return CommonCharacteristics.mapIfExists(uuid) ?: CustomCharacteristic.mapIfExists(uuid)
    }
}

enum class BondState(val bondState: Int) {
    BONDED(BluetoothDevice.BOND_BONDED),
    BONDING(BluetoothDevice.BOND_BONDING),
    NOT_BOND(BluetoothDevice.BOND_NONE);

    companion object {
        private val map = BondState.values().associateBy(BondState::bondState)
        fun get(bondState: Int) = map[bondState]
    }
}

enum class DeviceType(val deviceType: Int) {
    CLASSIC(BluetoothDevice.DEVICE_TYPE_CLASSIC),
    DUAL(BluetoothDevice.DEVICE_TYPE_DUAL),
    LE(BluetoothDevice.DEVICE_TYPE_LE),
    UNKNOWN_TYPE(BluetoothDevice.DEVICE_TYPE_UNKNOWN);

    companion object {
        private val map = DeviceType.values().associateBy(DeviceType::deviceType)
        fun get(deviceType: Int) = map[deviceType]
    }
}

sealed class ConnectionStatus {
    object CONNECTED : ConnectionStatus()
    object CONNECTING : ConnectionStatus()
    data class DISCONNECTED(val reason: String) : ConnectionStatus() {
        override fun toString(): String {
            return if (reason.length > 0)
                "${super.toString()}-$reason"
            else
                super.toString()
        }
    }

    object DISCONNECTING : ConnectionStatus()

    override fun toString(): String {
        return javaClass.simpleName
    }

    companion object {
        fun get(connectionStatus: Int, reason: String = ""): ConnectionStatus {
            return when (connectionStatus) {
                BluetoothProfile.STATE_CONNECTED -> CONNECTED
                BluetoothProfile.STATE_CONNECTING -> CONNECTING
                BluetoothProfile.STATE_DISCONNECTED -> DISCONNECTED(reason)
                BluetoothProfile.STATE_DISCONNECTING -> DISCONNECTING
                else -> DISCONNECTED("unknown State:$connectionStatus,reason:$reason")
            }
        }
    }
}

sealed class DiscoveryStatus {
    object STARTED : DiscoveryStatus()
    object NOT_DISCOVERED : DiscoveryStatus()
    data class DISCOVERED(val services: List<BluetoothGattService>) : DiscoveryStatus() {
        override fun toString(): String {
            return super.toString()
        }
    }

    data class FAILED(val reason: String) : DiscoveryStatus() {
        override fun toString(): String {
            return if (reason.length > 0)
                "${super.toString()}-$reason"
            else
                super.toString()
        }
    }

    override fun toString(): String {
        return this::class.java.simpleName
    }
}

enum class BluetoothGattStatus(val gattStatus: Int) {
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
    BROADCAST(BluetoothGattCharacteristic.PROPERTY_BROADCAST),
    EXTENDED_PROPS(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS),
    INDICATE(BluetoothGattCharacteristic.PROPERTY_INDICATE),
    NOTIFY(BluetoothGattCharacteristic.PROPERTY_NOTIFY),
    READ(BluetoothGattCharacteristic.PROPERTY_READ),
    SIGNED_WRITE(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE),
    WRITE(BluetoothGattCharacteristic.PROPERTY_WRITE),
    WRITE_NO_RESPONSE(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);

    fun isInCharacteristic(gattCharacteristic: BluetoothGattCharacteristic) = gattCharacteristic.properties and bluetoothGattCharacteristicProperty != 0

    companion object {
        private val map = values().associateBy(BluetoothCharacteristicProperty::bluetoothGattCharacteristicProperty)
        fun transform(property: Int): List<BluetoothCharacteristicProperty> {
            return map.values.filter { it.bluetoothGattCharacteristicProperty and property != 0 }
        }
    }
}

