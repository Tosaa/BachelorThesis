package asaa.bachelor.bleconnector.connections.connection.multi

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.MutableLiveData
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.bt.common.CustomService
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import timber.log.Timber

data class ConnectionItem(val address: String, private val manager: BluetoothManager) : IStatusObserver {
    var asLiveData = MutableLiveData(this)

    val timeKeeper = TimeKeeper()

    var isSelected = false
        set(value) {
            asLiveData.postValue(this)
            field = value
        }
    var isReady = false
        set(value) {
            asLiveData.postValue(this)
            field = value
        }

    var connection = manager.connectionFor(address)

    var isObserving = false
        set(value) {
            asLiveData.postValue(this)
            field = value
        }

    fun connect() {
        connection = manager.connectionFor(address)
        if (!isObserving) {
            connection?.addObserver(this)
            isObserving = true
        }
        manager.connect(address)
        timeKeeper.start("connect")
    }


    fun requestMTU(mtu: Int): Boolean {
        val requestIsGood = if (isReady) {
            timeKeeper.start("request MTU")
            connection?.requestMtu(mtu) ?: false
        } else {
            false
        }
        if (!requestIsGood) {
            timeKeeper.end("could not request MTU")
        }
        return requestIsGood
    }

    fun writeConnectionInterval(interval: String): Boolean {
        val requestIsGood = if (isReady) {
            timeKeeper.start("write Interval:$interval")
            connection?.requestWrite(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.CONNECTION_INTERVAL_CHARACTERISTIC.uuid, interval) ?: false
        } else {
            false
        }
        if (!requestIsGood) {
            timeKeeper.end("could not request new Connection Interval")
        }
        return requestIsGood
    }

    fun disconnect() {
        timeKeeper.start("disconnect")
        connection?.disconnect()
    }

    fun readC1() {
        if (isReady) {
            timeKeeper.start("read1")
            connection?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC.uuid).let { response ->
                Timber.i("$address read Characteristic 1: $response")
                if (response == false) {
                    timeKeeper.end("read1 not possible")
                }
            }
        } else
            Timber.w("read Characteristic 1 is not possible because $address is not ready")
    }

    fun readC2() {
        if (isReady) {
            timeKeeper.start("read2")
            connection?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC_2.uuid).let { response ->
                Timber.i("$address read Characteristic 2: $response")
                if (response == false) {
                    timeKeeper.end("read2 not possible")
                }
            }
        } else
            Timber.w("read Characteristic 2 is not possible because $address is not ready")
    }

    override fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onReadCharacteristic(characteristic, value)
        val received = if (value.size < 10) {
            value.joinToString(separator = "") { it.toChar().toString() }
        } else {
            value.take(7).joinToString(separator = "") { it.toChar().toString() }.plus("...")
        }
        Timber.i("readCharacteristic:${characteristic.uuid} = $received")
        timeKeeper.end("read:$received")

    }

    override fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        super.onWriteCharacteristic(characteristic, value, status)
        when (status) {
            BluetoothGattStatus.GATT_SUCCESS -> {
                timeKeeper.end("write: completed")
            }
            else -> {
                timeKeeper.end(status.toString())
            }
        }
    }

    fun toggle() {
        isSelected = !isSelected
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        Timber.d("$address is $newStatus")
        when (newStatus) {
            is ConnectionStatus.CONNECTED -> {
                connection?.discoverServices()
                timeKeeper.log("connected")
            }
            is ConnectionStatus.DISCONNECTED -> {
                isReady = false
                timeKeeper.end("disconnected")
                if (isObserving) {
                    isObserving = false
                    connection?.removeObserver(this)
                }
            }
            else -> {
                timeKeeper.log("${newStatus}")
            }
        }
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        Timber.d("$address discovery: $newDiscoveryState")
        timeKeeper.log("$newDiscoveryState")
        isReady = when (newDiscoveryState) {
            is DiscoveryStatus.DISCOVERED -> {
                timeKeeper.end("connected & discovered")
                true
            }
            else -> {
                false
            }
        }
    }

    inner class TimeKeeper {
        private val commandsList = mutableListOf<String>()
        private var timeBegin = System.currentTimeMillis()
        private var durationSinceStart = 0L
        fun commands(): String = commandsList.joinToString { if (it.length > 20) it.substring(0, 17) + "..." else it }
        fun duration(): String = durationSinceStart.toString()
        fun start(cmd: String) {
            timeBegin = System.currentTimeMillis()
            commandsList.clear()
            commandsList.add(cmd)
            asLiveData.postValue(this@ConnectionItem)
        }

        fun log(cmd: String) {
            commandsList.add(cmd)
            asLiveData.postValue(this@ConnectionItem)
        }

        fun end(cmd: String) {
            durationSinceStart = System.currentTimeMillis() - timeBegin
            commandsList.add(cmd)
            asLiveData.postValue(this@ConnectionItem)
        }
    }
}

