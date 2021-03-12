package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import timber.log.Timber

private const val TAG = "LogableBluetoothGattCallback"

open class LogableBluetoothGattCallback : BluetoothGattCallback() {

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        Timber.i("onPhyUpdate: $gatt, $txPhy ,$rxPhy, $status")
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
        Timber.i("onPhyRead: $gatt, $txPhy ,$rxPhy, $status")
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicRead(gatt, characteristic, status)
        Timber.i("onCharacteristicRead: $gatt, $characteristic, $status")
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Timber.i("onCharacteristicWrite: $gatt, $characteristic, $status")
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicChanged(gatt, characteristic)
        Timber.i("onCharacteristicChanged: $gatt, $characteristic")
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorRead(gatt, descriptor, status)
        Timber.i("onDescriptorRead: $gatt, $descriptor, $status")
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        super.onDescriptorWrite(gatt, descriptor, status)
        Timber.i("onDescriptorWrite: $gatt, $descriptor, $status")
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
        Timber.i("onReliableWriteCompleted: $gatt, $status")
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
        Timber.i("onReadRemoteRssi: $gatt, $rssi, $status")
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        Timber.i("onMtuChanged: $gatt, $mtu, $status")
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Timber.i("onConnectionStateChange: $gatt, $status, $newState")
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        Timber.i("onServicesDiscovered: $gatt, $status")
    }
}