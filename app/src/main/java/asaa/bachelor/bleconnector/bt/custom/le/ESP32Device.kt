package asaa.bachelor.bleconnector.bt.custom.le

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

class ESP32Device(device: BluetoothDevice) : BluetoothLowEnergyDevice(device) {

    val observer = mutableListOf<ESP32DeviceObserver>()
    fun addObserver(esp32DeviceObserver: ESP32DeviceObserver) = observer.add(esp32DeviceObserver)
    fun removeObserver(esp32DeviceObserver: ESP32DeviceObserver) = observer.remove(esp32DeviceObserver)

    private var characteristicRead1: BluetoothGattCharacteristic? = null
    private var characteristicRead2: BluetoothGattCharacteristic? = null
    private var characteristicWriteWithoutResponse: BluetoothGattCharacteristic? = null
    private var characteristicWrite: BluetoothGattCharacteristic? = null
    private var characteristicNotify: BluetoothGattCharacteristic? = null
    private var characteristicIndicate: BluetoothGattCharacteristic? = null
    private var characteristicConnectionParameter: BluetoothGattCharacteristic? = null

    var characteristic1 = ""
        set(value) {
            observer.forEach { it.onCharacteristic1Changed(value) }
            field = value
        }
    var characteristic2 = ""
        set(value) {
            observer.forEach { it.onCharacteristic2Changed(value) }
            field = value
        }
    var writeStatus: WriteStatus = WriteStatus.DONE("")
        set(value) {
            observer.forEach { it.writeCommandStatusChanged(value) }
            field = value
        }
    var writeWithoutResponseStatus: WriteStatus = WriteStatus.DONE("")
        set(value) {
            observer.forEach { it.writeCommandStatusChanged(value) }
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
    var connectionParameter = ""

    override fun initializeCharacteristics() {
        super.initializeCharacteristics()
        characteristicRead1 = getCharacteristic(SERVICE_UUID, READ_1_UUID)
        characteristicRead2 = getCharacteristic(SERVICE_UUID, READ_2_UUID)
        characteristicWriteWithoutResponse = getCharacteristic(SERVICE_UUID, WRITE_WO_RESPONSE_UUID)
        characteristicWrite = getCharacteristic(SERVICE_UUID, WRITE_UUID)
        characteristicNotify = getCharacteristic(SERVICE_UUID, NOTIFY_UUID)
        characteristicIndicate = getCharacteristic(SERVICE_UUID, INDICATE_UUID)
        characteristicConnectionParameter = getCharacteristic(SERVICE_UUID, CONNECTION_INTERVAL_UUID)
    }

    fun readCharacteristic1() {
        characteristicRead1?.let { readCharacteristic(it) }
    }

    fun readCharacteristic2() {
        characteristicRead2?.let { readCharacteristic(it) }
    }

    fun writeWithoutResponse(payload: String) {
        writeWithoutResponseStatus = WriteStatus.STARTED
        characteristicWriteWithoutResponse.let { characteristic ->
            if (characteristic == null) {
                writeWithoutResponseStatus = WriteStatus.FAILED("characteristic not initialized")
                return
            }
            writeWithoutResponseStatus = if (writeCharacteristic(characteristic, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE, payload.toByteArray())) {
                WriteStatus.PENDING
            } else {
                WriteStatus.FAILED()
            }
        }
    }

    fun write(payload: String) {
        writeStatus = WriteStatus.STARTED
        characteristicWrite.let { characteristic ->
            if (characteristic == null) {
                writeStatus = WriteStatus.FAILED("characteristic not initialized")
                return
            }
            writeStatus = if (writeCharacteristic(characteristic, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, payload.toByteArray())) {
                WriteStatus.PENDING
            } else {
                WriteStatus.FAILED()
            }
        }
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

    }

    override fun onReadResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString(separator = "") { it.toChar().toString() } ?: ""
        when (characteristic) {
            characteristicRead1 -> characteristic1 = value
            characteristicRead2 -> characteristic2 = value
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
            characteristicWrite -> writeStatus = WriteStatus.DONE(value)
            characteristicWriteWithoutResponse -> writeWithoutResponseStatus = WriteStatus.DONE(value)
        }
    }

    override fun onNoitificationChangedResult(characteristic: BluetoothGattCharacteristic?, notificationStatus: NotificationStatus) {
        when (characteristic) {
            characteristicNotify -> notifyStatus = notificationStatus
            characteristicIndicate -> indicateStatus = notificationStatus
        }
    }

    companion object {
        val SERVICE_UUID = "26cb2f28-a4ba-49fc-856a-d57fe4d3dada"
        val READ_1_UUID = "fbdc45f2-8337-467b-8019-e7db05355215"
        val READ_2_UUID = "ab191949-a8c0-438b-81ce-90b97f1858a8"
        val WRITE_UUID = "f847b552-f7dc-42e9-9dad-8452d7ad7681"
        val WRITE_WO_RESPONSE_UUID = "59bf9b12-ba39-4983-89bc-e541c2091535"
        val NOTIFY_UUID = "1ee1d0fc-6f3c-4c6a-ac1c-c54d2a97f932"
        val INDICATE_UUID = "83157f66-7c91-431e-a037-7c2b9e594ef6"
        val CONNECTION_INTERVAL_UUID = "46ac40cc-7eaa-41a9-9964-956a984fd9c3"
    }

}


interface ESP32DeviceObserver {

    fun onCharacteristic1Changed(newValue: String)
    fun onCharacteristic2Changed(newValue: String)

    fun writeCommandStatusChanged(writeStatus: WriteStatus)
    fun notifyStatusChanged(notificationStatus: NotificationStatus)
    fun notifyValueChanged(newValue: String)
    fun indicateStatusChanged(notificationStatus: NotificationStatus)
    fun indicateValueChanged(newValue: String)
    fun connectionPropertyChanged()


}