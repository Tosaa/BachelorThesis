package asaa.bachelor.bleconnector.bt.manager

import android.bluetooth.BluetoothDevice

interface BluetoothManagerListener {

    fun onDeviceAdded(device: BluetoothDevice)
    fun onDiscoveryStarted()
    fun onDiscoveryStopped()
}