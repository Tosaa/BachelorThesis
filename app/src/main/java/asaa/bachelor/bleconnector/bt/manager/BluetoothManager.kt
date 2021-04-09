package asaa.bachelor.bleconnector.bt.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import asaa.bachelor.bleconnector.bt.custom.le.BluetoothLowEnergyDevice
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import asaa.bachelor.bleconnector.bt.IBluetoothOrchestrator
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothManager @Inject constructor(@ApplicationContext val context: Context) :
    IBluetoothOrchestrator {

    init {
        Timber.v("bluetooth Orchestrator created")
    }

    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    val btDevices: MutableList<BluetoothDevice> = mutableListOf()

    private val handler = Handler(context.mainLooper)

    private val btDeviceLowEnergyDeviceMap: MutableMap<BluetoothDevice, BluetoothLowEnergyDevice> =
        mutableMapOf()

    private fun resolveBTDevice(macAddress: String): BluetoothDevice? = btDevices.find { it.address == macAddress }
    private fun resolveBTConnection(macAddress: String): BluetoothLowEnergyDevice? {
        return resolveBTDevice(macAddress)?.let {
            btDeviceLowEnergyDeviceMap[it]
        }
    }

    private fun addDeviceToList(bluetoothDevice: BluetoothDevice) {
        if (!btDevices.contains(bluetoothDevice)) {
            notifyDiscoveryStarted(bluetoothDevice)
            btDevices.add(bluetoothDevice)
        }
    }

    // Callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Timber.v("onScanResult: ${result?.device?.address} [${result?.rssi}]")
            result?.device?.let {
                addDeviceToList(it)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Timber.v("onBatchResult")
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.w("err: $errorCode")
        }
    }

    private var isScanning = false

    // ScanFilters (need to be in a list or null)
    private val filter: List<ScanFilter>? = null

    // ScanSettings
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .build()

    override fun startLowEnergyDiscovery() {
        if (!isScanning) {
            Timber.i("Start Discovering Low Energy Devices")
            btAdapter.bluetoothLeScanner.startScan(filter, scanSettings, scanCallback)
            notifyDiscoveryStarted()
            handler.postDelayed({
                stopDiscovery()
            }, 10000L)
            isScanning = true
        } else {
            Timber.w("Start Discovering Low Energy Devices was called while Scanning")
        }
    }

    override fun stopDiscovery() {
        if (isScanning) {
            Timber.i("Stop Discovering Devices")
            handler.removeCallbacksAndMessages(null)
            notifyDiscoveryStoped()
            btAdapter.bluetoothLeScanner.stopScan(scanCallback)
            btAdapter.cancelDiscovery()
            isScanning = false
        } else {
            Timber.w("Stop Discovering Devices was called without Scanning in progress")
        }
    }

    fun startClassicDiscovery() {
        if (!isScanning) {
            Timber.i("Start Discovering Classic Devices")
            notifyDiscoveryStarted()
            btAdapter.startDiscovery()
            isScanning=true
        } else {
            Timber.w("Start Discovering classic Devices was called while Scanning")
        }
    }

    fun addBluetoothDevice(btDevice: BluetoothDevice) {
        addDeviceToList(btDevice)
    }

    // TODO: To be removed
    override fun connect(macAddress: String): BluetoothLowEnergyDevice? {
        Timber.v("request connection for: $macAddress")
        val device = resolveBTDevice(macAddress)
        if (device == null) {
            Timber.v("No Device for address: $macAddress")
            return null
        }
        if (btDeviceLowEnergyDeviceMap[device] == null) {
            Timber.v("create new BluetoothConnection for: $macAddress")
            btDeviceLowEnergyDeviceMap[device] = BluetoothLowEnergyDevice(device)
        }
        Timber.v("connect BluetoothDevice: $macAddress")
        btDeviceLowEnergyDeviceMap[device]?.connect(context, false)

        return btDeviceLowEnergyDeviceMap[device]
    }

    // Todo: To be changed to connectionFor(bluetoothDevice: BluetoothDevice):BluetoothConnection
    fun connectionFor(macAddress: String): BluetoothLowEnergyDevice? {
        btDevices.find { it.address == macAddress }?.let {
            if (it.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN || it.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                Timber.v("try to get Connection for non ble device: ${it.address}")
                return null
            }
            if (btDeviceLowEnergyDeviceMap[it] == null) {
                btDeviceLowEnergyDeviceMap[it] = BluetoothLowEnergyDevice(it)
            }
            return btDeviceLowEnergyDeviceMap[it]
        } ?: return null
    }

    // Todo: To be removed
    override fun disconnect(macAddress: String) {
        Timber.v("disconnect: $macAddress")
        val connection = resolveBTConnection(macAddress)
        if (connection == null || connection.connectionStatus != ConnectionStatus.CONNECTED) {
            Timber.v("questionable disconnect call for connection: $connection")
            return
        }
        connection.disconnect()
    }

    // Todo: to be removed
    fun disconnectAll() {
        btDeviceLowEnergyDeviceMap.forEach { (device, connection) ->
            if (connection.connectionStatus == ConnectionStatus.CONNECTED) {
                Timber.v("disconnect: ${device.address}")
                connection.disconnect()
            }
        }
    }

    private val observer = mutableListOf<BluetoothManagerListener>()
    fun addObserver(bluetoothManagerListener: BluetoothManagerListener) {
        if (!observer.contains(bluetoothManagerListener))
            observer.add(bluetoothManagerListener)
    }

    fun removeObserver(bluetoothManagerListener: BluetoothManagerListener) {
        observer.remove(bluetoothManagerListener)
    }

    fun notifyDiscoveryStarted() {
        observer.forEach {
            it.onDiscoveryStarted()
        }
    }

    fun notifyDiscoveryStoped() {
        observer.forEach {
            it.onDiscoveryStopped()
        }
    }

    fun notifyDiscoveryStarted(bluetoothDevice: BluetoothDevice) {
        observer.forEach {
            it.onDeviceAdded(bluetoothDevice)
        }
    }

}