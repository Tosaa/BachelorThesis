package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import timber.log.Timber
import kotlin.system.measureTimeMillis

private const val TAG = "LogableBluetoothGattCallback"

open class LogableBluetoothGattCallback : BluetoothGattCallback() {

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        Timber.i("onPhyUpdate: $gatt, $txPhy ,$rxPhy, $status, ${System.currentTimeMillis()}")
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        Timber.i("onPhyRead: $gatt, $txPhy ,$rxPhy, $status, ${System.currentTimeMillis()}")
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        Timber.i("onCharacteristicRead: $gatt, $characteristic, $status, ${System.currentTimeMillis()}")
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Timber.i("onCharacteristicWrite: $gatt, $characteristic, $status, ${System.currentTimeMillis()}")
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        Timber.i("onCharacteristicChanged: $gatt, $characteristic, ${System.currentTimeMillis()}")
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorRead(gatt, descriptor, status)
        Timber.i("onDescriptorRead: $gatt, $descriptor, $status, ${System.currentTimeMillis()}")
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        Timber.i("onDescriptorWrite: $gatt, $descriptor, $status, ${System.currentTimeMillis()}")
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
        Timber.i("onReliableWriteCompleted: $gatt, $status, ${System.currentTimeMillis()}")
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        Timber.i("onReadRemoteRssi: $gatt, $rssi, $status, ${System.currentTimeMillis()}")
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        Timber.i("onMtuChanged: $gatt, $mtu, $status, ${System.currentTimeMillis()}")
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Timber.i("onConnectionStateChange: $gatt, $status, $newState, ${System.currentTimeMillis()}")
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Timber.i("onServicesDiscovered: $gatt, $status, ${System.currentTimeMillis()}")
    }
}