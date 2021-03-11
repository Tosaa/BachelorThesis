package asaa.bachelor.bleconnector.bt

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.util.*

private const val TAG: String = "BluetoothConnection"
const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"

class BluetoothConnection(val device: BluetoothDevice) {


    init {
        Timber.v("create BluetoothConnection for $device")
    }

    var bluetoothGatt: BluetoothGatt? = null

    // States
    var connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED("")
        set(value) {
            notifyConnectionStateChanged(value)
            Timber.v("connection Status Changed: $field -> $value")
            field = value
        }

    var discoveryStatus: DiscoveryStatus = DiscoveryStatus.NOT_DISCOVERED
        set(value) {
            notifyDiscoveryStateChanged(value)
            Timber.v("discovery Status Changed: $field -> $value")
            field = value
        }

    var bondState: BondState = BondState.get(device.bondState) ?: BondState.NOT_BOND
        set(value) {
            notifyBondStateChanged(value)
            Timber.v("bond Status Changed: $field -> $value")
            field = value
        }

    // observer
    val observers: MutableList<IStatusObserver> = mutableListOf()

    // callback will trigger all observers and change state values
    val callback = BluetoothCallback()


    fun addObserver(o: IStatusObserver) {
        Timber.v("add Observer: $o")
        observers.add(o)
        o.onConnectionStateChanged(connectionStatus)
        o.onDiscoveryStateChanged(discoveryStatus)
        o.onBondStateChanged(bondState)
    }

    fun removeObserver(o: IStatusObserver) {
        observers.remove(o)
    }

    private fun notifyConnectionStateChanged(status: ConnectionStatus) {
        observers.forEach {
            it.onConnectionStateChanged(status)
        }
    }

    private fun notifyDiscoveryStateChanged(status: DiscoveryStatus) {
        observers.forEach {
            it.onDiscoveryStateChanged(status)
        }
    }

    private fun notifyBondStateChanged(status: BondState) {
        observers.forEach {
            it.onBondStateChanged(status)
        }
    }

    private fun notifyOnRead(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        observers.forEach {
            it.onReadCharacteristic(characteristic, value)
        }
    }

    private fun notifyOnWrite(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        observers.forEach {
            it.onWriteCharacteristic(characteristic, value, status)
        }
    }


    // operations on BluetoothConnection

    fun requestWrite(service: String, characteristic: String, value: String): Boolean {
        Timber.v("write: $value on  $service -> $characteristic")
        val gattCharacteristic = getCharacteristic(service, characteristic) ?: return false
        val properties = BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)
        val writeType = when {
            properties.contains(BluetoothCharacteristicProperty.WRITE) -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            properties.contains(BluetoothCharacteristicProperty.WRITE_NO_RESPONSE) -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> {
                Timber.v("characteristic: $gattCharacteristic has not property write or write no response:${BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)}")
                return false
            }
        }
        return writeCharacteristic(gattCharacteristic, writeType, value.toByteArray())
    }

    private fun writeCharacteristic(gattCharacteristic: BluetoothGattCharacteristic, writeType: Int, value: ByteArray): Boolean {
        Timber.v("writeCharacteristic: $gattCharacteristic, $writeType, $value")
        bluetoothGatt?.let { gatt ->
            gattCharacteristic.writeType = writeType
            gattCharacteristic.value = value
            gatt.writeCharacteristic(gattCharacteristic)
            return true
        }
        Timber.w("could not write $value to characteristic")
        return false
    }

    private fun getCharacteristic(service: String, characteristic: String): BluetoothGattCharacteristic? {
        val serviceUUID = UUID.fromString(service)
        val characteristicUUID = UUID.fromString(characteristic)
        val gattCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        if (gattCharacteristic == null)
            Timber.w("could not find Characteristic of $service - $characteristic for device: $device")
        return gattCharacteristic
    }

    fun requestRead(service: String, characteristic: String): Boolean {
        Timber.v("requestRead: $service -> $characteristic")
        val gattCharacteristic = getCharacteristic(service, characteristic) ?: return false
        return requestRead(gattCharacteristic)
    }

    fun requestRead(gattCharacteristic: BluetoothGattCharacteristic): Boolean {
        Timber.v("requestRead: $gattCharacteristic")
        if (BluetoothCharacteristicProperty.READ.isInCharacteristic(gattCharacteristic)) {
            readCharacteristic(gattCharacteristic)
            return true
        }
        Timber.v("characteristic: $gattCharacteristic has not property read:${BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)}")
        return false

    }

    private fun readCharacteristic(gattCharacteristic: BluetoothGattCharacteristic) {
        Timber.v("readCharacteristic: $gattCharacteristic ")
        bluetoothGatt?.readCharacteristic(gattCharacteristic)
    }


    fun requestStopNotifyOrIndicate(service: String, characteristic: String): Boolean {
        Timber.v("requestStopNotifyOrIndicate:  $service -> $characteristic")
        val gattCharacteristic = getCharacteristic(service, characteristic) ?: return false
        val descriptor = gattCharacteristic.getDescriptor(UUID.fromString(CCCD_UUID)) ?: return false
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
    }

    fun requestStartIndicate(service: String, characteristic: String): Boolean {
        Timber.v("requestStartIndicate:  $service -> $characteristic")
        val gattCharacteristic = getCharacteristic(service, characteristic) ?: return false
        val descriptor = gattCharacteristic.getDescriptor(UUID.fromString(CCCD_UUID)) ?: return false
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
    }

    fun requestStartNotify(service: String, characteristic: String): Boolean {
        Timber.v("requestStartNotify:  $service -> $characteristic")
        val gattCharacteristic = getCharacteristic(service, characteristic) ?: return false

        val descriptor = gattCharacteristic.getDescriptor(UUID.fromString(CCCD_UUID)) ?: return false
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }

    private fun changeNotificationStatus(gattCharacteristic: BluetoothGattCharacteristic, descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
        Timber.v("changeNotificationStatus: $gattCharacteristic, $descriptor, ${BtUtil.readableByteArray(payload)}")
        when (payload) {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE -> {
                if (BluetoothCharacteristicProperty.NOTIFY.isInCharacteristic(gattCharacteristic)) {
                    if (bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) == false) {
                        Timber.w("changeNotificationStatus failed for ${gattCharacteristic.uuid}")
                        return false
                    }
                    return writeDescriptor(descriptor, payload)
                }
            }
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE -> {
                if (BluetoothCharacteristicProperty.INDICATE.isInCharacteristic(gattCharacteristic)) {
                    if (bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) == false) {
                        Timber.w("changeNotificationStatus failed for ${gattCharacteristic.uuid}")
                        return false
                    }
                    return writeDescriptor(descriptor, payload)
                }
            }
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE -> {
                if (BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)?.let {
                        it.contains(BluetoothCharacteristicProperty.NOTIFY) || it.contains(BluetoothCharacteristicProperty.INDICATE)
                    }) {
                    if (bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) == false) {
                        Timber.w("changeNotificationStatus failed for ${gattCharacteristic.uuid}")
                        return false
                    }
                    return writeDescriptor(descriptor, payload)
                }
            }
            else -> Timber.w("changeNotificationStatus: $gattCharacteristic has no Notify or Indicate Properties:${BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)}")
        }
        Timber.w("changeNotificationStatus failed for $gattCharacteristic, $${BtUtil.readableByteArray(payload)}")
        return false
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
        Timber.v("writeDescriptor: $descriptor, ${BtUtil.readableByteArray(payload)}")
        bluetoothGatt?.let {
            descriptor.value = payload
            it.writeDescriptor(descriptor)
            return true
        }
        Timber.w("could not write ${BtUtil.readableByteArray(payload)} to $descriptor")
        return false
    }


    /*
    * connect() should start discovery too.
    * if service discovery failed it can be retried.
    *
    * */
    fun discoverServices() {
        Handler(Looper.getMainLooper()).run {
            if (discoveryStatus != DiscoveryStatus.STARTED && discoveryStatus is DiscoveryStatus.DISCOVERED)
                bluetoothGatt?.discoverServices()
        }
    }

    fun connect(context: Context, autoConnect: Boolean) {
        Timber.v("connect $device (autoconnect:$autoConnect)")
        connectionStatus = ConnectionStatus.CONNECTING
        device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    inner class BluetoothCallback : LogableBluetoothGattCallback() {

        init {
            Timber.v("Create new BluetoothGattCallback for this(${device.address}) connection")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.let {
                notifyOnRead(it, it.value)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            discoveryStatus = DiscoveryStatus.DISCOVERED(gatt?.services ?: emptyList())
            Timber.v(gatt?.services?.joinToString {
                "${it.uuid.toString()}(${BtUtil.serviceToString(it.uuid.toString())})"
            })
            gatt?.services?.forEach {
                Timber.v(
                    it.characteristics.joinToString {
                        "${it.uuid.toString()}(${BtUtil.characteristicToString(it.uuid.toString())})"
                    })
            }

            connectionStatus = ConnectionStatus.CONNECTED
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            val gattStatus = BluetoothGattStatus.get(status)
            Timber.v("gatt status: $gattStatus")
            when (gattStatus) {
                BluetoothGattStatus.GATT_SUCCESS -> {
                    Timber.v("value was written: " + characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not write value")
                }
                BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Timber.v("not permitted to write value")
                }
                BluetoothGattStatus.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    Timber.v("The written Value was to big for the current ATT MTU size")
                }
                else -> {
                }
            }

            characteristic?.let {
                notifyOnWrite(it, it?.value ?: emptyArray<Byte>().toByteArray(), gattStatus ?: BluetoothGattStatus.GATT_FAILURE)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            val gattStatus = BluetoothGattStatus.get(status)
            Timber.v("gatt status: $gattStatus")

            when (gattStatus) {
                BluetoothGattStatus.GATT_SUCCESS -> {
                    Timber.v("value: " + characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not read value")
                }
                BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Timber.v("not permitted to read value")
                }
                else -> {
                }
            }
            characteristic?.let {
                notifyOnRead(it, it?.value ?: emptyArray<Byte>().toByteArray())
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt == null) {
                Timber.v("gatt is null: $gatt")
                return
            }
            bluetoothGatt = gatt
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Timber.w("Successfully connected to ${device.address}")
                    discoveryStatus = DiscoveryStatus.STARTED
                    Handler(Looper.getMainLooper()).run {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Timber.w("Successfully disconnected from ${device.address}")
                    gatt.close()
                }
                connectionStatus = ConnectionStatus.get(newState)
            } else {
                Timber.w(
                    "Error $status encountered for ${device.address}! Disconnecting..."
                )
                connectionStatus = ConnectionStatus.get(newState, status.toString())
                gatt.close()
            }
        }
    }

}