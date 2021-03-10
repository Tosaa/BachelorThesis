package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import java.util.*

private const val TAG: String = "BluetoothConnection"

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

    private fun notifyOnRead(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        observers.forEach {
            it.onReadCharacteristic(characteristic, value, status)
        }
    }


    inner class BluetoothCallback : LogableBluetoothGattCallback() {

        init {
            Timber.v("Create new BluetoothGattCallback for this(${device.address}) connection")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            discoveryStatus = DiscoveryStatus.DISCOVERED(gatt?.services ?: emptyList())
            gatt?.services?.forEach {
                Timber.v(
                    it.characteristics.joinToString(
                        separator = ",",
                    ) { it.uuid.toString() }
                )
            }
            connectionStatus = ConnectionStatus.CONNECTED
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
                notifyOnRead(it, it?.value ?: emptyArray<Byte>().toByteArray(), gattStatus ?: BluetoothGattStatus.GATT_FAILURE)
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

    // operations on BluetoothConnection

    fun readCharacteristic(service: String, characteristic: String): Boolean {
        Timber.v("read $service -> $characteristic")
        val serviceUUID = UUID.fromString(service)
        val characteristicUUID = UUID.fromString(characteristic)
        val gattCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        return if (BluetoothCharacteristicProperty.transform(gattCharacteristic?.properties ?: 0).contains(BluetoothCharacteristicProperty.READ)) {
            Timber.v("start readCharacteristic")
            bluetoothGatt?.readCharacteristic(gattCharacteristic)
            true
        } else {
            Timber.v("characteristic: $gattCharacteristic is not readable()")
            false
        }
    }

    fun readCharacteristic(bluetoothGattCharacteristic: BluetoothGattCharacteristic): Boolean {
        if (bluetoothGatt?.services?.any { it.characteristics.contains(bluetoothGattCharacteristic) } == true) {
            bluetoothGatt?.readCharacteristic(bluetoothGattCharacteristic)
            return true
        }
        return false
    }

    /*
    * if service discovery failed it can be retried
    *
    * */
    fun discoverServices() {
        Handler(Looper.getMainLooper()).run {
            if (discoveryStatus != DiscoveryStatus.STARTED && discoveryStatus is DiscoveryStatus.DISCOVERED)
                bluetoothGatt?.discoverServices()
        }
    }

    fun writeCharacteristic() {}

    fun connect(context: Context, autoConnect: Boolean) {
        Timber.v("connect $device (autoconnect:$autoConnect)")
        connectionStatus = ConnectionStatus.CONNECTING
        device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

}