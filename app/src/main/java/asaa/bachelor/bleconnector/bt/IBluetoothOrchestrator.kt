package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

interface IBluetoothOrchestrator {

    fun startDiscovery()
    fun stopDiscovery()
    fun connect(macAddress: String): BluetoothConnection?
    fun disconnect(macAddress: String)

}

interface IStatusObserver {
    fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {}
    fun onWriteCharacteristic() {}
    fun onConnectionStateChanged(newStatus: ConnectionStatus) {}
    fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {}
    fun onBondStateChanged(bond: BondState) {}
}

