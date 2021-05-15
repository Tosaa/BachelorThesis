package asaa.bachelor.bleconnector.connections.connection.multi

import android.content.Context
import androidx.lifecycle.MutableLiveData
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.custom.le.ESP32Device
import asaa.bachelor.bleconnector.bt.custom.le.ESP32DeviceObserver
import asaa.bachelor.bleconnector.bt.custom.le.NotificationStatus
import asaa.bachelor.bleconnector.bt.custom.le.WriteStatus
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import timber.log.Timber

data class ConnectionItem(val address: String, private val manager: BluetoothManager) : DeviceStateObserver, ESP32DeviceObserver {
    var asLiveData = MutableLiveData(this)

    val timeKeeper = TimeKeeper()

    var readValue = ""
        set(value) {
            field = value
            asLiveData.postValue(this)
        }

    var isSelected = false
        set(value) {
            field = value
            asLiveData.postValue(this)
        }

    var isReady = false
        set(value) {
            field = value
            asLiveData.postValue(this)
        }

    var notifyValue = ""
        set(value) {
            field = value
            asLiveData.postValue(this)
        }

    var indicateValue = ""
        set(value) {
            field = value
            asLiveData.postValue(this)
        }

    var connection = manager.getCustomBluetoothDeviceFor(address) as ESP32Device?

    fun refresh() {
        if (connection == null) {
            connection = manager.getCustomBluetoothDeviceFor(address) as ESP32Device?
        }
        if (connection != null) {
            asLiveData.postValue(this)
        }
    }

    var isObserving = false
        set(value) {
            asLiveData.postValue(this)
            field = value
        }

    fun connect(context: Context) {
        if (!isObserving) {
            connection?.addGeneralObserver(this)
            connection?.addObserver(this)
            isObserving = true
        }
        connection?.connect(context, false)
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
        if (isReady) {
            timeKeeper.start("write Interval:$interval")
            connection?.changeConnectionParameter(interval.toInt())
        }
        return false
    }

    fun disconnect() {
        timeKeeper.start("disconnect")
        connection?.disconnect()
    }

    fun readC1() {
        if (isReady) {
            timeKeeper.start("read1")
            connection?.readCharacteristic1()
        } else
            Timber.w("read Characteristic 1 is not possible because $address is not ready")
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
                    connection?.removeGeneralObserver(this)
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

    override fun onCharacteristic1Changed(newValue: String) {
        timeKeeper.end(newValue)
        readValue = newValue
    }

    fun toggleNotify() {
        if (isReady) {

            if (connection?.notifyStatus is NotificationStatus.DONE && (connection?.notifyStatus as NotificationStatus.DONE).isActive) {
                timeKeeper.start("Stop Notify")
                connection?.stopNotify()
            } else {
                timeKeeper.start("Start Notify")
                connection?.startNotify()
            }
        } else {
            Timber.w("device not ready")
        }
    }

    override fun notifyStatusChanged(notificationStatus: NotificationStatus) {
        super.notifyStatusChanged(notificationStatus)
        if (notificationStatus is NotificationStatus.DONE || notificationStatus is NotificationStatus.FAILED) {
            timeKeeper.end(notificationStatus.toString())
        } else {
            timeKeeper.log(notificationStatus.toString())
        }
    }

    override fun notifyValueChanged(newValue: String) {
        super.notifyValueChanged(newValue)
        notifyValue = newValue
    }

    fun toggleIndicate() {
        if (isReady) {
            if (connection?.indicateStatus is NotificationStatus.DONE && (connection?.indicateStatus as NotificationStatus.DONE).isActive) {
                timeKeeper.start("Stop Indicate")
                connection?.stopIndicate()
            } else {
                timeKeeper.start("Start Indicate")
                connection?.startIndicate()
            }
        } else {
            Timber.w("device not ready")
        }
    }

    override fun indicateStatusChanged(notificationStatus: NotificationStatus) {
        super.indicateStatusChanged(notificationStatus)
        if (notificationStatus is NotificationStatus.DONE || notificationStatus is NotificationStatus.FAILED) {
            timeKeeper.end(notificationStatus.toString())
        } else {
            timeKeeper.log(notificationStatus.toString())
        }
    }

    override fun indicateValueChanged(newValue: String) {
        super.indicateValueChanged(newValue)
        indicateValue = newValue
    }

    override fun connectionPropertyChanged(mtu: Int, connectionInterval: Int, phy: Int) {
        super.connectionPropertyChanged(mtu, connectionInterval, phy)
        timeKeeper.end("connection updated")
        Timber.i("connection property changed: mtu: $mtu, conn-interval: $connectionInterval, phy: $phy")
    }

    fun updatePhy(phyLevel: PhyLevel) {
        timeKeeper.start("phy level: ${phyLevel.name}")
        connection?.updatePhy(phyLevel)
    }
}

