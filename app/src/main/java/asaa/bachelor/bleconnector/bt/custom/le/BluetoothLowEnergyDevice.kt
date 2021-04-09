package asaa.bachelor.bleconnector.bt.custom.le

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CommonDescriptors
import asaa.bachelor.bleconnector.bt.custom.CustomBluetoothDevice
import timber.log.Timber
import java.util.*

abstract class BluetoothLowEnergyDevice(device: BluetoothDevice) : CustomBluetoothDevice(device) {

    private var bluetoothGatt: BluetoothGatt? = null

    // callback will trigger all observers and change state values
    private val callback = BluetoothCallback()

    // States
    override var connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED("")
        set(value) {
            notifyConnectionStateChanged(value)
            Timber.d("$deviceTag: connection Status Changed: $field -> $value")
            if (value is ConnectionStatus.DISCONNECTED) {
                bluetoothGatt = null
            }
            field = value
        }

    var discoveryStatus: DiscoveryStatus = DiscoveryStatus.NOT_DISCOVERED
        set(value) {
            notifyDiscoveryStateChanged(value)
            Timber.d("$deviceTag: discovery Status Changed: $field -> $value")
            field = value
        }

    var bondState: BondState = BondState.get(device.bondState) ?: BondState.NOT_BOND
        set(value) {
            notifyBondStateChanged(value)
            Timber.d("$deviceTag: bond Status Changed: $field -> $value")
            field = value
        }

    // observer
    private val generalObserver: MutableList<IStatusObserver> = mutableListOf()

    fun addGeneralObserver(o: IStatusObserver) {
        Timber.d("$deviceTag: add general Observer: $o")
        generalObserver.add(o)
        o.onConnectionStateChanged(connectionStatus)
        o.onDiscoveryStateChanged(discoveryStatus)
        o.onBondStateChanged(bondState)
    }

    fun removeGeneralObserver(o: IStatusObserver) {
        Timber.d("$deviceTag: remove general Observer: $o")
        generalObserver.remove(o)
    }

    private fun notifyConnectionStateChanged(status: ConnectionStatus) {
        generalObserver.forEach {
            it.onConnectionStateChanged(status)
        }
    }

    private fun notifyDiscoveryStateChanged(status: DiscoveryStatus) {
        generalObserver.forEach {
            it.onDiscoveryStateChanged(status)
        }
    }

    private fun notifyBondStateChanged(status: BondState) {
        generalObserver.forEach {
            it.onBondStateChanged(status)
        }
    }

    private fun notifyOnRead(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        generalObserver.forEach {
            it.onReadCharacteristic(characteristic, value)
        }
    }

    private fun notifyOnWrite(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        generalObserver.forEach {
            it.onWriteCharacteristic(characteristic, value, status)
        }
    }


    // operations on BluetoothConnection

    fun requestMtu(mtu: Int): Boolean {
        Timber.d("$deviceTag: request Mtu: $mtu")
        bluetoothGatt?.let {
            return it.requestMtu(mtu)
        }
        return false
    }


    internal fun writeCharacteristic(gattCharacteristic: BluetoothGattCharacteristic, writeType: Int, value: ByteArray): Boolean {
        Timber.d("$deviceTag: writeCharacteristic: $gattCharacteristic, $writeType, $value")
        bluetoothGatt?.let { gatt ->
            gattCharacteristic.writeType = writeType
            gattCharacteristic.value = value
            gatt.writeCharacteristic(gattCharacteristic)
            return true
        }
        Timber.w("$deviceTag: could not write $value to characteristic")
        return false
    }

    internal fun getCharacteristic(service: String, characteristic: String): BluetoothGattCharacteristic? {
        val serviceUUID = UUID.fromString(service)
        val characteristicUUID = UUID.fromString(characteristic)
        val gattCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        Timber.d("$deviceTag: resolve Characteristic: $service - $characteristic - $gattCharacteristic")
        if (gattCharacteristic == null)
            Timber.w("$deviceTag: could not find Characteristic of $service - $characteristic")
        return gattCharacteristic
    }

    internal fun readCharacteristic(gattCharacteristic: BluetoothGattCharacteristic) {
        Timber.d("$deviceTag: readCharacteristic: $gattCharacteristic ")
        val apiReturnValue = bluetoothGatt?.readCharacteristic(gattCharacteristic)
        Timber.d("$deviceTag: readCharacteristic api response: $apiReturnValue")
    }

    internal fun requestStopNotifyOrIndicate(gattCharacteristic: BluetoothGattCharacteristic): Boolean {
        val descriptor = gattCharacteristic.getDescriptor(CommonDescriptors.ClientCharacteristicConfiguration.uuid) ?: return false
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
    }

    internal fun requestStartIndicate(gattCharacteristic: BluetoothGattCharacteristic): Boolean {
        val descriptor = gattCharacteristic.getDescriptor(CommonDescriptors.ClientCharacteristicConfiguration.uuid)
        if (descriptor == null) {
            Timber.w("$deviceTag: descriptor for Indicate (CCCD) was not found on characteristic: ${gattCharacteristic.descriptors}")
            return false
        }
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
    }

    internal fun requestStartNotify(gattCharacteristic: BluetoothGattCharacteristic): Boolean {
        val descriptor = gattCharacteristic.getDescriptor(CommonDescriptors.ClientCharacteristicConfiguration.uuid)
        if (descriptor == null) {
            Timber.w("$deviceTag: descriptor for Notify (CCCD) was not found on characteristic: ${gattCharacteristic.descriptors}")
            return false
        }
        return changeNotificationStatus(gattCharacteristic, descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }

    private fun changeNotificationStatus(gattCharacteristic: BluetoothGattCharacteristic, descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
        Timber.d("$deviceTag: changeNotificationStatus: $gattCharacteristic, $descriptor, ${BtUtil.readableByteArray(payload)}")
        when (payload) {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE -> {
                if (BluetoothCharacteristicProperty.NOTIFY.isInCharacteristic(gattCharacteristic)) {
                    if (bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) == false) {
                        Timber.w("$deviceTag: changeNotificationStatus failed for ${gattCharacteristic.uuid}")
                        return false
                    }
                    return writeDescriptor(descriptor, payload)
                }
            }
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE -> {
                if (BluetoothCharacteristicProperty.INDICATE.isInCharacteristic(gattCharacteristic)) {
                    if (bluetoothGatt?.setCharacteristicNotification(gattCharacteristic, true) == false) {
                        Timber.w("$deviceTag: changeNotificationStatus failed for ${gattCharacteristic.uuid}")
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
                        Timber.w("$deviceTag: changeNotificationStatus failed for ${gattCharacteristic.uuid}")
                        return false
                    }
                    return writeDescriptor(descriptor, payload)
                }
            }
            else -> Timber.w("$deviceTag: changeNotificationStatus: $gattCharacteristic has no Notify or Indicate Properties:${BluetoothCharacteristicProperty.transform(gattCharacteristic.properties)}")
        }
        Timber.w("$deviceTag: changeNotificationStatus failed for $gattCharacteristic, $${BtUtil.readableByteArray(payload)}")
        return false
    }

    private fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray): Boolean {
        Timber.d("$deviceTag: writeDescriptor: $descriptor, ${BtUtil.readableByteArray(payload)}")
        bluetoothGatt?.let {
            descriptor.value = payload
            it.writeDescriptor(descriptor)
            return true
        }
        Timber.w("$deviceTag: could not write ${BtUtil.readableByteArray(payload)} to $descriptor")
        return false
    }


    /*
    * connect() should start discovery too.
    * if service discovery failed it can be retried.
    *
    * */
    fun discoverServices() {
        Timber.i("$deviceTag: Discover Services")
        Handler(Looper.getMainLooper()).run {
            if (discoveryStatus != DiscoveryStatus.STARTED && !(discoveryStatus is DiscoveryStatus.DISCOVERED))
                bluetoothGatt?.discoverServices()
            else
                Timber.w("$deviceTag: Services cant be discovered")
        }
    }

    fun connect(context: Context, autoConnect: Boolean) {
        Timber.i("$deviceTag: connect (autoconnect:$autoConnect)")
        connectionStatus = ConnectionStatus.CONNECTING
        device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        Timber.i("$deviceTag: disconnect")
        bluetoothGatt?.disconnect()
    }


    abstract fun onReadResult(characteristic: BluetoothGattCharacteristic?)
    abstract fun onWriteResult(characteristic: BluetoothGattCharacteristic?)

    inner class BluetoothCallback : LogableBluetoothGattCallback() {

        init {
            Timber.d("$deviceTag: Create new BluetoothGattCallback for this connection")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Timber.d("$deviceTag: onDescriptorRead: $gatt, ${BluetoothGattStatus.get(status)}")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Timber.d("$deviceTag: onDescriptorWrite: $gatt, ${BluetoothGattStatus.get(status)}")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Timber.d("$deviceTag: onCharacteristicChanged: $gatt")
            characteristic?.let {
                notifyOnRead(it, it.value)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Timber.d("$deviceTag: onServicesDiscovered: status ${BluetoothGattStatus.get(status)}")
            discoveryStatus = DiscoveryStatus.DISCOVERED(gatt?.services ?: emptyList())
            Timber.d(gatt?.services?.joinToString {
                "${it.uuid}(${BtUtil.serviceToString(it.uuid.toString())})"
            })
            gatt?.services?.forEach {
                Timber.d(
                    it.characteristics.joinToString {
                        "${it.uuid}(${BtUtil.characteristicToString(it.uuid.toString())})"
                    })
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            val gattStatus = BluetoothGattStatus.get(status)
            Timber.d("$deviceTag: onCharacteristicWrite: status $gattStatus")
            when (gattStatus) {
                BluetoothGattStatus.GATT_SUCCESS -> {
                    onWriteResult(characteristic)
                    Timber.d("$deviceTag: value was written: " + characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not write value")
                }
                BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Timber.d("$deviceTag: not permitted to write value")
                }
                BluetoothGattStatus.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    Timber.d("$deviceTag: The written Value was to big for the current ATT MTU size")
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
            Timber.d("$deviceTag: onCharacteristicRead: status $gattStatus")

            when (gattStatus) {
                BluetoothGattStatus.GATT_SUCCESS -> {
                    onReadResult(characteristic)
                    Timber.d("$deviceTag: value: " + characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not read value")
                }
                BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Timber.d("$deviceTag: not permitted to read value")
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
            val gattStatus = BluetoothGattStatus.get(status)
            Timber.d("$deviceTag: onConnectionStateChanged: status $gattStatus")
            if (gatt == null) {
                Timber.w("$deviceTag: gatt is null: $gatt")
                return
            }
            bluetoothGatt = gatt
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Timber.i("$deviceTag: Successfully connected")

                    /*

                        discoveryStatus = DiscoveryStatus.STARTED
                        Handler(Looper.getMainLooper()).run {
                            gatt.discoverServices()
                        }
                        */
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    discoveryStatus = DiscoveryStatus.NOT_DISCOVERED
                    Timber.w("$deviceTag: Successfully disconnected")
                    gatt.close()
                }
                connectionStatus = ConnectionStatus.get(newState)
            } else {
                Timber.w(
                    "$deviceTag Error $status encountered for Disconnecting..."
                )
                connectionStatus = ConnectionStatus.get(newState, status.toString())
                gatt.close()
            }
        }
    }

}

sealed class NotificationStatus {
    object STARTED : NotificationStatus()
    object PENDING : NotificationStatus()
    class DONE(val isActive: Boolean) : NotificationStatus()
    class FAILED(val info: String = "") : NotificationStatus()
}

sealed class WriteStatus {
    object STARTED : WriteStatus()
    object PENDING : WriteStatus()
    class DONE(val writtenValue: String) : WriteStatus()
    class FAILED(val info: String = "") : WriteStatus()
}