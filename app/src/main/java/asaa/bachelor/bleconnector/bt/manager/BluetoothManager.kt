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
import asaa.bachelor.bleconnector.bt.custom.CustomBluetoothDevice
import asaa.bachelor.bleconnector.bt.custom.classic.CustomClassicDevice
import asaa.bachelor.bleconnector.bt.custom.le.CustomLowEnergyDevice
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
    val customBluetoothDevices = mutableListOf<CustomBluetoothDevice>()

    private val handler = Handler(context.mainLooper)

    private fun addDeviceToList(bluetoothDevice: BluetoothDevice) {
        if (!btDevices.contains(bluetoothDevice)) {
            notifyDiscoveryFound(bluetoothDevice)
            btDevices.add(bluetoothDevice)
            when (bluetoothDevice.type) {
                BluetoothDevice.DEVICE_TYPE_LE -> customBluetoothDevices.add(CustomLowEnergyDevice(bluetoothDevice))
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> customBluetoothDevices.add(CustomClassicDevice(bluetoothDevice))
            }
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
            isScanning = true
        } else {
            Timber.w("Start Discovering classic Devices was called while Scanning")
        }
    }

    fun addBluetoothDevice(btDevice: BluetoothDevice) {
        addDeviceToList(btDevice)
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

    fun notifyDiscoveryFound(bluetoothDevice: BluetoothDevice) {
        observer.forEach {
            it.onDeviceAdded(bluetoothDevice)
        }
    }

}