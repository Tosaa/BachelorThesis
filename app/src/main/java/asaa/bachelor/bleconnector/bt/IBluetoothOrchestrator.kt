package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothGattService
import java.util.*

interface IBluetoothOrchestrator {

    fun startDiscovery()
    fun stopDiscovery()
    fun connect(macAddress: String): BluetoothConnection?
    fun disconnect(macAddress: String)

}

interface IStatusObserver {
    fun onReadCharacteristic() {}
    fun onWriteCharacteristic() {}
    fun onStatusChanged(newStatus: ConnectionStatus) {}
    fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {}
}

sealed class ConnectionStatus {

    object NOT_CONNECTED : ConnectionStatus()
    object CONNECTING : ConnectionStatus()
    object CONNECTED : ConnectionStatus()
    data class CONNECTING_FAILED(val reason: String) : ConnectionStatus()
    object DISCONNECTING : ConnectionStatus()
    object DISCONNECTED : ConnectionStatus()

    override fun toString(): String {
        return this::class.java.simpleName
    }
}

sealed class DiscoveryStatus {
    object DISCOVERY_STARTED : DiscoveryStatus()
    data class DISCOVERY_FINISHED(val services: List<BluetoothGattService>) : DiscoveryStatus()
    data class DISCOVERY_FAILED(val reason: String) : DiscoveryStatus()

    override fun toString(): String {
        return this::class.java.simpleName
    }
}

