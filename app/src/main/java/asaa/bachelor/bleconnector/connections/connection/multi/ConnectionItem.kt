package asaa.bachelor.bleconnector.connections.connection.multi

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.MutableLiveData
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.bt.common.CustomService
import timber.log.Timber

data class ConnectionItem(val address: String, private val orchestrator: BluetoothOrchestrator) : IStatusObserver {
    var asLiveData = MutableLiveData(this)

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
    val latestCommands = mutableListOf<String>()
    var latestCommandDuration = "--"
        set(value) {
            asLiveData.postValue(this)
            field = value
        }
    var time = System.currentTimeMillis()
        set(value) {
            asLiveData.postValue(this)
            field = value
        }
    var connection = orchestrator.connectionFor(address)

    var isObserving = false
        set(value) {
            asLiveData.postValue(this)
            field = value
        }

    fun connect() {
        connection = orchestrator.connectionFor(address)
        if (!isObserving) {
            connection?.addObserver(this)
            isObserving = true
        }
        time = System.currentTimeMillis()
        orchestrator.connect(address)
        latestCommands.clear()
        latestCommands.add("connection:start")
    }

    fun disconnect() {
        connection?.disconnect()
    }

    fun readC1() {
        if (isReady) {
            time = System.currentTimeMillis()
            latestCommands.clear()
            latestCommands.add("read1:start")
            connection?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC.uuid).let { response ->
                Timber.i("$address read Characteristic 1: $response")
            }
        } else
            Timber.w("read Characteristic 1 is not possible because $address is not ready")
    }

    fun readC2() {
        if (isReady) {
            time = System.currentTimeMillis()
            latestCommands.clear()
            latestCommands.add("read1:start")
            connection?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC_2.uuid).let { response ->
                Timber.i("$address read Characteristic 2: $response")
                if (response == false) {
                    latestCommands.add("read: not possible")
                    latestCommandDuration = (System.currentTimeMillis() - time).toString()
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
        latestCommands.add("read:$received")
        latestCommandDuration = (System.currentTimeMillis() - time).toString()

    }

    fun toggle() {
        isSelected = !isSelected
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        Timber.d("$address is $newStatus")
        latestCommands.add("connection:${newStatus}")
        latestCommandDuration = (System.currentTimeMillis() - time).toString()

        if (newStatus is ConnectionStatus.CONNECTED) {
            connection?.discoverServices()
            latestCommands.add("discovery:start")
        }
        if (newStatus is ConnectionStatus.DISCONNECTED) {
            isReady = false
            if (isObserving) {
                isObserving = false
                connection?.removeObserver(this)
            }
        }
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        Timber.d("$address discovery: $newDiscoveryState")
        latestCommands.add("discovery:${newDiscoveryState.toString()}")
        latestCommandDuration = (System.currentTimeMillis() - time).toString()
        isReady = newDiscoveryState is DiscoveryStatus.DISCOVERED
    }
}

