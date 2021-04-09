package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothGattCharacteristic

interface DeviceStateObserver {
    fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {}
    fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {}
    fun onConnectionStateChanged(newStatus: ConnectionStatus) {}
    fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {}
    fun onBondStateChanged(bond: BondState) {}
}