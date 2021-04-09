package asaa.bachelor.bleconnector.bt.custom.le

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

class CustomLowEnergyDevice(device: BluetoothDevice) : BluetoothLowEnergyDevice(device) {

    val observer = mutableListOf<CustomLowEnergyDeviceObserver>()
    fun addObserver(customLowEnergyDeviceObserver: CustomLowEnergyDeviceObserver) = observer.add(customLowEnergyDeviceObserver)
    fun removeObserver(customLowEnergyDeviceObserver: CustomLowEnergyDeviceObserver) = observer.remove(customLowEnergyDeviceObserver)

    private val characteristicRead1 = getCharacteristic(SERVICE_UUID, READ_1_UUID)
    private val characteristicRead2 = getCharacteristic(SERVICE_UUID, READ_2_UUID)
    private val characteristicWriteWithoutResponse = getCharacteristic(SERVICE_UUID, WRITE_WO_RESPONSE_UUID)
    private val characteristicWrite = getCharacteristic(SERVICE_UUID, WRITE_UUID)
    private val characteristicNotify = getCharacteristic(SERVICE_UUID, NOTIFY_UUID)
    private val characteristicIndicate = getCharacteristic(SERVICE_UUID, INDICATE_UUID)
    private val characteristicConnectionParameter = getCharacteristic(SERVICE_UUID, CONNECTION_INTERVAL_UUID)


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
    var notifyStatus: NotificationStatus = NotificationStatus.DONE(false)
    var indicateStatus: NotificationStatus = NotificationStatus.DONE(false)
    var connectionParameter = ""

    fun readCharacteristic1() {
        if (characteristicRead1 != null) {
            readCharacteristic(characteristicRead1)
        }
    }

    fun readCharacteristic2() {
        if (characteristicRead2 != null) {
            readCharacteristic(characteristicRead2)
        }
    }

    fun writeWithoutResponse(payload: String) {
        writeWithoutResponseStatus = WriteStatus.STARTED
        if (characteristicWriteWithoutResponse == null) {
            writeWithoutResponseStatus = WriteStatus.FAILED("characteristic not initialized")
            return
        }
        writeWithoutResponseStatus = if (writeCharacteristic(characteristicWriteWithoutResponse, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE, payload.toByteArray())) {
            WriteStatus.PENDING
        } else {
            WriteStatus.FAILED()
        }
    }

    fun write(payload: String) {
        writeStatus = WriteStatus.STARTED
        if (characteristicWrite == null) {
            writeStatus = WriteStatus.FAILED("characteristic not initialized")
            return
        }
        writeStatus = if (writeCharacteristic(characteristicWrite, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, payload.toByteArray())) {
            WriteStatus.PENDING
        } else {
            WriteStatus.FAILED()
        }
    }

    fun startNotify() {
        notifyStatus = NotificationStatus.STARTED
        if (characteristicNotify == null) {
            notifyStatus = NotificationStatus.FAILED("characteristic not initialized")
            return
        }
        notifyStatus = if (requestStartNotify(characteristicNotify)) {
            NotificationStatus.PENDING
        } else {
            NotificationStatus.FAILED()
        }
    }

    fun stopNotify() {
        notifyStatus = NotificationStatus.STARTED
        if (characteristicNotify == null) {
            notifyStatus = NotificationStatus.FAILED("characteristic not initialized")
            return
        }
        notifyStatus = if (requestStopNotifyOrIndicate(characteristicNotify)) {
            NotificationStatus.PENDING
        } else {
            NotificationStatus.FAILED()
        }
    }

    fun startIndicate() {
        indicateStatus = NotificationStatus.STARTED
        if (characteristicIndicate == null) {
            indicateStatus = NotificationStatus.FAILED("characteristic not initialized")
            return
        }
        indicateStatus = if (requestStartIndicate(characteristicIndicate)) {
            NotificationStatus.PENDING
        } else {
            NotificationStatus.FAILED()
        }
    }

    fun stopIndicate() {
        indicateStatus = NotificationStatus.STARTED
        if (characteristicIndicate == null) {
            indicateStatus = NotificationStatus.FAILED("characteristic not initialized")
            return
        }
        indicateStatus = if (requestStopNotifyOrIndicate(characteristicIndicate)) {
            NotificationStatus.PENDING
        } else {
            NotificationStatus.FAILED()
        }
    }

    fun changeConnectionParameter(interval:Int){

    }

    override fun onReadResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString() ?: ""
        when (characteristic) {
            characteristicRead1 -> characteristic1 = value
            characteristicRead2 -> characteristic2 = value

        }
    }

    override fun onWriteResult(characteristic: BluetoothGattCharacteristic?) {
        val value = characteristic?.value?.joinToString() ?: ""
        when (characteristic) {
            characteristicWrite -> writeStatus = WriteStatus.DONE(value)
            characteristicWriteWithoutResponse -> writeWithoutResponseStatus = WriteStatus.DONE(value)
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


interface CustomLowEnergyDeviceObserver {

    fun onCharacteristic1Changed(newValue: String)
    fun onCharacteristic2Changed(newValue: String)

    fun writeCommandStatusChanged(writeStatus: WriteStatus)
    fun notifyStatusChanged(notificationStatus: NotificationStatus)
    fun indicateStatusChanged(notificationStatus: NotificationStatus)
    fun connectionPropertyChanged()


}