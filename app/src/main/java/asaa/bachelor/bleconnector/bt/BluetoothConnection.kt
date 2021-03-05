package asaa.bachelor.bleconnector.bt

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

private const val TAG: String = "BluetoothConnection"

class BluetoothConnection(private val device: BluetoothDevice) {

    init {
        Log.v(TAG, "create BluetoothConnection for $device")
    }

    var connectionStatus: ConnectionStatus = ConnectionStatus.NOT_CONNECTED
        set(value) {
            notifyStatusChanged(value)
            Log.v(TAG, "connection Status Changed: $field -> $value")
            field = value
        }
    var discoveryStatus: DiscoveryStatus = DiscoveryStatus.DISCOVERY_FAILED("not discovered yet")
        set(value) {
            notifyDiscoveryStatusChanged(value)
            Log.v(TAG, "discovery Status Changed: $field -> $value")
            field = value
        }

    var bluetoothGatt: BluetoothGatt? = null
    val observers: MutableList<IStatusObserver> = mutableListOf()
    val callback = BluetoothCallback()

    fun connect(context: Context, autoConnect: Boolean) {
        Log.v(TAG, "connect $device (autoconnect:$autoConnect)")
        connectionStatus = ConnectionStatus.CONNECTING
        device.connectGatt(context, autoConnect, callback)
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    fun addObserver(o: IStatusObserver) {
        Log.v(TAG, "add Observer: $o")
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
            Log.v(TAG, "Create new BluetoothGattCallback for this(${device.address}) connection")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            discoveryStatus = DiscoveryStatus.DISCOVERY_FINISHED(gatt?.services ?: emptyList())
            gatt?.services?.forEach {
                Log.v(TAG,
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
            Log.v(TAG, "gatt status: $gattStatus")
            when (gattStatus) {
                BtUtil.BluetoothGattStatus.GATT_SUCCESS -> {
                    Log.v(TAG, characteristic?.value?.joinToString(" ") { it.toChar().toString() } ?: "could not read value")
                }
                BtUtil.BluetoothGattStatus.GATT_READ_NOT_PERMITTED -> {
                    Log.v(TAG, "not permitted to read value")
                }
                else -> {
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (gatt == null) {
                Log.v(TAG, "gatt is null: $gatt")
                return
            }
            bluetoothGatt = gatt
            connectionStatus = BtUtil.resolveBluetoothProfileToConnectionStatus(newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w(TAG, "Successfully connected to ${device.address}")
                    discoveryStatus = DiscoveryStatus.DISCOVERY_STARTED
                    Handler(Looper.getMainLooper()).run {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w(TAG, "Successfully disconnected from ${device.address}")
                    gatt.close()
                }
            } else {
                Log.w(
                    TAG,
                    "Error $status encountered for ${device.address}! Disconnecting..."
                )
                connectionStatus = ConnectionStatus.CONNECTING_FAILED("$status")
                gatt.close()
            }
        }
    }


    fun readCharacteristic(service: String, characteristic: String): Boolean {
        Log.v(TAG, "read $service -> $characteristic")
        val serviceUUID = UUID.fromString(service)
        val characteristicUUID = UUID.fromString(characteristic)
        val gattCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(characteristicUUID)
        return if (BtUtil.BluetoothCharacteristicProperty.transform(gattCharacteristic?.properties ?: 0).contains(BtUtil.BluetoothCharacteristicProperty.PROPERTY_READ)) {
            Log.v(TAG, "start readCharacteristic")
            bluetoothGatt?.readCharacteristic(gattCharacteristic)
            true
        } else {
            Log.v(TAG, "characteristic: $gattCharacteristic is not readable()")
            false
        }
    }

    fun writeCharacteristic() {}

}