package asaa.bachelor.bleconnector.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothOrchestrator @Inject constructor(@ApplicationContext val context: Context) :
    IBluetoothOrchestrator {

    init {
        Timber.v("bluetooth Orchestrator created")
    }

    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    val btDevices: MutableList<BluetoothDevice> = mutableListOf()

    private val handler = Handler(context.mainLooper)

    private val btDeviceConnectionMap: MutableMap<BluetoothDevice, BluetoothConnection> =
        mutableMapOf()

    private fun resolveBTDevice(macAddress: String): BluetoothDevice? = btDevices.find { it.address == macAddress }
    private fun resolveBTConnection(macAddress: String): BluetoothConnection? {
        return resolveBTDevice(macAddress)?.let {
            btDeviceConnectionMap[it]
        }
    }

    private fun addDeviceToList(bluetoothDevice: BluetoothDevice) {
        if (!btDevices.contains(bluetoothDevice)) {
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

    // ScanFilters (need to be in a list or null)
    private val filter: List<ScanFilter>? = null

    // ScanSettings
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .build()

    override fun startDiscovery() {
        Timber.i("Start Discovery Devices")
        btAdapter.bluetoothLeScanner.startScan(filter, scanSettings, scanCallback)
        handler.postDelayed({
            stopDiscovery()
        }, 10000L)
    }

    override fun stopDiscovery() {
        Timber.i("Stop Discovery Devices")
        handler.removeCallbacksAndMessages(null)
        btAdapter.bluetoothLeScanner.stopScan(scanCallback)
        btAdapter.cancelDiscovery()
    }

    fun startClassicDiscovery() {
        Timber.i("Start Discovery Classic Devices")
        btAdapter.startDiscovery()
    }

    fun addBluetoothDevice(btDevice: BluetoothDevice) {
        addDeviceToList(btDevice)
    }

    override fun connect(macAddress: String): BluetoothConnection? {
        Timber.v("request connection for: $macAddress")
        val device = resolveBTDevice(macAddress)
        if (device == null) {
            Timber.v("No Device for address: $macAddress")
            return null
        }
        if (btDeviceConnectionMap[device] == null) {
            Timber.v("create new BluetoothConnection for: $macAddress")
            btDeviceConnectionMap[device] = BluetoothConnection(device)
        }
        Timber.v("connect BluetoothDevice: $macAddress")
        btDeviceConnectionMap[device]?.connect(context, false)

        return btDeviceConnectionMap[device]
    }

    fun connectionFor(macAddress: String): BluetoothConnection? {
        btDevices.find { it.address == macAddress }?.let {
            if (it.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN || it.type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                Timber.v("try to get Connection for non ble device: ${it.address}")
                return null
            }
            if (btDeviceConnectionMap[it] == null) {
                btDeviceConnectionMap[it] = BluetoothConnection(it)
            }
            return btDeviceConnectionMap[it]
        } ?: return null
    }

    override fun disconnect(macAddress: String) {
        Timber.v("disconnect: $macAddress")
        val connection = resolveBTConnection(macAddress)
        if (connection == null || connection.connectionStatus != ConnectionStatus.CONNECTED) {
            Timber.v("questionable disconnect call for connection: $connection")
            return
        }
        connection.disconnect()
    }

    fun disconnectAll() {
        btDeviceConnectionMap.forEach { (device, connection) ->
            if (connection.connectionStatus == ConnectionStatus.CONNECTED) {
                Timber.v("disconnect: ${device.address}")
                connection.disconnect()
            }
        }
    }


}