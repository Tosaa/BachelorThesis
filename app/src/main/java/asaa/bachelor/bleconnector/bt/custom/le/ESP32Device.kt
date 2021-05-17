package asaa.bachelor.bleconnector.bt.custom.le

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import timber.log.Timber
import java.nio.ByteBuffer

class ESP32Device(device: BluetoothDevice) : BluetoothLowEnergyDevice(device) {

    val observer = mutableListOf<ESP32DeviceObserver>()
    fun addObserver(esp32DeviceObserver: ESP32DeviceObserver) = observer.add(esp32DeviceObserver)
    fun removeObserver(esp32DeviceObserver: ESP32DeviceObserver) = observer.remove(esp32DeviceObserver)

    private var characteristicRead1: BluetoothGattCharacteristic? = null
    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicIndicate: BluetoothGattCharacteristic? = null
    private var characteristicConnectionParameter: BluetoothGattCharacteristic? = null
    private var characteristicSizeParameter: BluetoothGattCharacteristic? = null
    private var characteristicCommand: BluetoothGattCharacteristic? = null

    var characteristic1 = ""
        set(value) {
            observer.forEach { it.onCharacteristic1Changed(value) }
            field = value
        }
    var notifyStatus: NotificationStatus = NotificationStatus.DONE(false)
        set(value) {
            observer.forEach { it.notifyStatusChanged(value) }
            field = value
        }
    var notifyValue: String = ""
        set(value) {
            observer.forEach { it.notifyValueChanged(value) }
            field = value
        }
    var indicateStatus: NotificationStatus = NotificationStatus.DONE(false)
        set(value) {
            observer.forEach { it.indicateStatusChanged(value) }
            field = value
        }
    var indicateValue: String = ""
        set(value) {
            observer.forEach { it.indicateValueChanged(value) }
            field = value
        }
    private var latestMtu = 23
        set(value) {
            field = value
            observer.forEach { it.connectionPropertyChanged(latestMtu, latestInterval, latestPhy) }
        }
    private var latestInterval = 0
        set(value) {
            field = value
            observer.forEach { it.connectionPropertyChanged(latestMtu, latestInterval, latestPhy) }
        }
    private var latestPhy = 0
        set(value) {
            field = value
            observer.forEach { it.connectionPropertyChanged(latestMtu, latestInterval, latestPhy) }
        }
    private var latestSize = 0
        set(value) {
            field = value
            observer.forEach { it.dataSizeChanged(value) }
        }

    override fun initializeCharacteristics() {
        super.initializeCharacteristics()
        characteristicRead1 = getCharacteristic(SERVICE_UUID, READ_1_UUID)
        characteristicNotify = getCharacteristic(SERVICE_UUID, NOTIFY_UUID)
        characteristicIndicate = getCharacteristic(SERVICE_UUID, INDICATE_UUID)
        characteristicConnectionParameter = getCharacteristic(SERVICE_UUID, CONNECTION_INTERVAL_UUID)
        characteristicCommand = getCharacteristic(SERVICE_UUID, COMMAND_UUID)
        characteristicSizeParameter = getCharacteristic(SERVICE_UUID, DATA_SIZE_UUID)
    }

    fun readCharacteristic1() {
        characteristicRead1?.let { readCharacteristic(it) }
    }

    fun startNotify() {
        notifyStatus = NotificationStatus.STARTED
        characteristicNotify.let { characteristic ->
            if (characteristic == null) {
                notifyStatus = NotificationStatus.FAILED("characteristic not initialized")
                return
            }
            notifyStatus = if (requestStartNotify(characteristic)) {
                NotificationStatus.PENDING
            } else {
                NotificationStatus.FAILED()
            }
        }
    }

    fun stopNotify() {
        notifyStatus = NotificationStatus.STARTED
        characteristicNotify.let { characteristic ->
            if (characteristic == null) {
                notifyStatus = NotificationStatus.FAILED("characteristic not initialized")
                return
            }
            notifyStatus = if (requestStopNotifyOrIndicate(characteristic)) {
                NotificationStatus.PENDING
            } else {
                NotificationStatus.FAILED()
            }
        }
    }

    fun startIndicate() {
        indicateStatus = NotificationStatus.STARTED
        characteristicIndicate.let { characteristic ->
            if (characteristic == null) {
                indicateStatus = NotificationStatus.FAILED("characteristic not initialized")
                return
            }
            indicateStatus = if (requestStartIndicate(characteristic)) {
                NotificationStatus.PENDING
            } else {
                NotificationStatus.FAILED()
            }
        }
    }

    fun stopIndicate() {
        indicateStatus = NotificationStatus.STARTED
        characteristicIndicate.let { characteristic ->
            if (characteristic == null) {
                indicateStatus = NotificationStatus.FAILED("characteristic not initialized")
                return
            }
            indicateStatus = if (requestStopNotifyOrIndicate(characteristic)) {
                NotificationStatus.PENDING
            } else {
                NotificationStatus.FAILED()
            }
        }
    }

    fun changeConnectionParameter(interval: Int) {
        characteristicConnectionParameter.let { characteristic ->
            if (characteristic == null) {
                return
            }

            val byteArray = ByteBuffer.allocate(4).putInt(interval).array().reversedArray()
            Timber.i("update connection params to: ${byteArray.toUByteArray().joinToString()} ")
            writeCharacteristic(characteristic, value = byteArray)
        }

    }

    fun changeDataSize(newSize: Int) {
        characteristicSizeParameter.let { characteristic ->
            if (characteristic == null) {
                return
            }

            val byteArray = ByteBuffer.allocate(4).putInt(newSize).array().reversedArray()
            Timber.i("update value size: ${byteArray.toUByteArray().joinToString()} ")
            writeCharacteristic(characteristic, value = byteArray)
        }
    }

    fun sentReadCommand() {
        characteristicCommand.let { characteristic ->
            if (characteristic == null) {
                return
            }

            writeCharacteristic(characteristic, value = "READ".toByteArray())
        }
    }

    override fun onDisconnected() {
        super.onDisconnected()
        characteristicRead1 = null
        characteristicNotify = null
        characteristicIndicate = null
        characteristicConnectionParameter = null
        characteristicSizeParameter = null
        characteristicCommand = null
        notifyStatus = NotificationStatus.DONE(false)
        indicateStatus = NotificationStatus.DONE(false)
    }

    override fun onReadResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString(separator = "") { it.toChar().toString() } ?: ""
        when (characteristic) {
            characteristicRead1 -> characteristic1 = value
        }
    }

    override fun onCharacteristicChangedResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString(separator = "") { it.toChar().toString() } ?: ""
        when (characteristic) {
            characteristicNotify -> notifyValue = value
            characteristicIndicate -> indicateValue = value
        }
    }

    override fun onWriteResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString(separator = "") { it.toChar().toString() } ?: ""
        when (characteristic) {
            characteristicCommand -> Timber.d("Command was send")
            characteristicSizeParameter -> latestSize = characteristic?.value?.first()?.toUByte()?.toInt() ?: 20
            characteristicConnectionParameter -> latestInterval = characteristic?.value?.take(2)?.mapIndexed { index, byte ->
                if (index == 0)
                    byte.toUByte().toInt()
                else
                    byte.toUByte().toInt() * index * 256
            }?.sum() ?: 0
        }
    }

    override fun onNotificationChangedResult(characteristic: BluetoothGattCharacteristic?, notificationStatus: NotificationStatus) {
        when (characteristic) {
            characteristicNotify -> notifyStatus = notificationStatus
            characteristicIndicate -> indicateStatus = notificationStatus
        }
    }

    override fun onPhyUpdated(txPhy: Int) {
        super.onPhyUpdated(txPhy)
        latestPhy = txPhy
    }

    override fun onMtuUpdated(mtu: Int) {
        super.onMtuUpdated(mtu)
        latestMtu = mtu
    }

    companion object {
        val SERVICE_UUID = "26cb2f28-a4ba-49fc-856a-d57fe4d3dada"
        val READ_1_UUID = "fbdc45f2-8337-467b-8019-e7db05355215"
        val NOTIFY_UUID = "1ee1d0fc-6f3c-4c6a-ac1c-c54d2a97f932"
        val INDICATE_UUID = "83157f66-7c91-431e-a037-7c2b9e594ef6"
        val CONNECTION_INTERVAL_UUID = "46ac40cc-7eaa-41a9-9964-956a984fd9c3"
        val COMMAND_UUID = "8c65f73d-ddab-4dd9-a2e0-f5a10ce7e252"
        val DATA_SIZE_UUID = "f3410822-00c0-4dd3-a60b-3cd6124fd323"
    }

}


interface ESP32DeviceObserver {

    fun onCharacteristic1Changed(newValue: String) {}

    fun notifyStatusChanged(notificationStatus: NotificationStatus) {}
    fun notifyValueChanged(newValue: String) {}
    fun indicateStatusChanged(notificationStatus: NotificationStatus) {}
    fun indicateValueChanged(newValue: String) {}
    fun connectionPropertyChanged(mtu: Int, connectionInterval: Int, phy: Int) {}
    fun dataSizeChanged(newSize: Int) {}


}