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
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BluetoothOrchestrator"

@Singleton
class BluetoothOrchestrator @Inject constructor(@ApplicationContext val context: Context) :
    IBluetoothOrchestrator {

    init {
        Log.v(TAG, "bluetooth Orchestrator created")
    }

    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    val btDevices: MutableList<BluetoothDevice> = mutableListOf()

    val handler = Handler(context.mainLooper)

    private val btDeviceConnectionMap: MutableMap<BluetoothDevice, BluetoothConnection> =
        mutableMapOf()

    private fun resolveBTDevice(macAddress: String): BluetoothDevice? = btDevices.find { it.address == macAddress }
    private fun resolveBTConnection(macAddress: String): BluetoothConnection? {
        return resolveBTDevice(macAddress)?.let {
            btDeviceConnectionMap[it]
        }
    }

    // Callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.v(TAG, "onScanResult:$result")
            result?.device?.let {
                if (!btDevices.contains(it)) {
                    btDevices.add(it)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.v(TAG, "onBatchResult")
        }

        override fun onScanFailed(errorCode: Int) {
            Logger.getLogger("BluetoothOrchestrator").warning("err: $errorCode")
        }
    }

    // ScanFilters (need to be in a list or null)
    val filter: List<ScanFilter>? = null

    // ScanSettings
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .build()

    override fun startDiscovery() {
        btAdapter.bluetoothLeScanner.startScan(filter, scanSettings, scanCallback)
        handler.postDelayed({
            stopDiscovery()
        }, 10000L)
    }

    override fun stopDiscovery() {
        handler.removeCallbacksAndMessages(null)
        btAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    override fun connect(macAddress: String): BluetoothConnection? {
        Log.v(TAG, "request connection for: $macAddress")
        val device = resolveBTDevice(macAddress)
        if (device == null) {
            Log.v(TAG, "No Device for Addr: $macAddress")
            return null
        }
        if (btDeviceConnectionMap[device] == null) {
            Log.v(TAG, "create new BluetoothConnection for: $macAddress")
            btDeviceConnectionMap[device] = BluetoothConnection(device)
        }
        Log.v(TAG, "connect BluetoothDevice: $macAddress")
        btDeviceConnectionMap[device]?.connect(context, false)

        return btDeviceConnectionMap[device]
    }

    fun connectionFor(macAddress: String): BluetoothConnection? {
        return btDeviceConnectionMap[btDevices.find { it.address == macAddress } ?: return null]
    }

    override fun disconnect(macAddress: String) {
        Log.v(TAG, "disconnect:$macAddress")
        val connection = resolveBTConnection(macAddress)
        if (connection == null || connection.connectionStatus != ConnectionStatus.CONNECTED) {
            Log.v(TAG, "questionable disconnect call for connection: $connection")
            return
        }
        connection.disconnect()
    }

    fun disconnectAll() {
        btDeviceConnectionMap.forEach { device, connection ->
            if (connection.connectionStatus == ConnectionStatus.CONNECTED) {
                Log.v(TAG, "disconnect: ${device.address}")
                connection.disconnect()
            }
        }
    }


}