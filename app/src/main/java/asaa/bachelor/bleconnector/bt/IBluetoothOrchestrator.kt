package asaa.bachelor.bleconnector.bt

import asaa.bachelor.bleconnector.bt.custom.le.BluetoothLowEnergyDevice

interface IBluetoothOrchestrator {

    fun startLowEnergyDiscovery()
    fun stopDiscovery()
    fun connect(macAddress: String): BluetoothLowEnergyDevice?
    fun disconnect(macAddress: String)

}

