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

class BluetoothConnection(private val device: BluetoothDevice) {

    init {
        Timber.v("create BluetoothConnection for $device")
    }

    var connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED
        set(value) {
            notifyStatusChanged(value)
            Timber.v("connection Status Changed: $field -> $value")
            field = value
        }
    var discoveryStatus: DiscoveryStatus = DiscoveryStatus.DISCOVERY_FAILED("not discovered yet")
        set(value) {
            notifyDiscoveryStatusChanged(value)
            Timber.v("discovery Status Changed: $field -> $value")
            field = value
        }

    var bluetoothGatt: BluetoothGatt? = null
    val observers: MutableList<IStatusObserver> = mutableListOf()
    val callback = BluetoothCallback()

    fun connect(context: Context, autoConnect: Boolean) {
        Timber.v("connect $device (autoconnect:$autoConnect)")
        connectionStatus = ConnectionStatus.CONNECTING
        device.connectGatt(context, autoConnect, callback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun addObserver(o: IStatusObserver) {
        Timber.v("add Observer: $o")
        observers.add(o)
        o.onStatusChanged(connectionStatus)
        o.onDiscoveryStateChanged(discoveryStatus)
    }

    fun removeObserver(o: IStatusObserver) {
        observers.remove(o)
    }

    private fun notifyStatusChanged(status: ConnectionStatus) {
        observers.forEach {
            it.onStatusChanged(status)
        }
    }

    private fun notifyDiscoveryStatusChanged(status: DiscoveryStatus) {
        observers.forEach {
            it.onDiscoveryStateChanged(status)
        }
    }


    inner class BluetoothCallback : LogableBluetoothGattCallback() {

        init {
            Timber.v("Create new BluetoothGattCallback for this(${device.address}) connection")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            discoveryStatus = DiscoveryStatus.DISCOVERY_FINISHED(gatt?.services ?: emptyList())
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
            val gattStatus = BtUtil.BluetoothGattStatus.get(status)
            Timber.v("gatt status: $gattStatus")
            when (gattStatus) {
                BtUtil.BluetoothGattStatus.GATT_SUCCESS -> {
                    Timber.v(characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not read value")
                }
                BtUtil.BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Timber.v("not permitted to read value")
                }
                else -> {
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt == null) {
                Timber.v("gatt is null: $gatt")
                return
            }
            bluetoothGatt = gatt
            connectionStatus = BtUtil.resolveBluetoothProfileToConnectionStatus(newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Timber.w("Successfully connected to ${device.address}")
                    discoveryStatus = DiscoveryStatus.DISCOVERY_STARTED
                    Handler(Looper.getMainLooper()).run {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Timber.w("Successfully disconnected from ${device.address}")
                    gatt.close()
                }
            } else {
                Timber.w(
                    "Error $status encountered for ${device.address}! Disconnecting..."
                )
                connectionStatus = ConnectionStatus.CONNECTING_FAILED("$status")
                gatt.close()
            }
        }
    }


    fun readCharacteristic(service: String, characteristic: String): Boolean {
        Timber.v("read $service -> $characteristic")
        val serviceUUID = UUID.fromString(service)
        val characteristicUUID = UUID.fromString(characteristic)
        val gattCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        return if (BtUtil.BluetoothCharacteristicProperty.transform(gattCharacteristic?.properties ?: 0).contains(BtUtil.BluetoothCharacteristicProperty.PROPERTY_READ)) {
            Timber.v("start readCharacteristic")
            bluetoothGatt?.readCharacteristic(gattCharacteristic)
            true
        } else {
            Timber.v("characteristic: $gattCharacteristic is not readable()")
            false
        }
    }

    fun writeCharacteristic() {}

}