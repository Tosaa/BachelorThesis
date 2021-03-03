package asaa.bachelor.bleconnector.bt

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.net.MacAddress
import android.os.Handler
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

const val TAG = "BluetoothOrchestrator"

@Singleton
class BluetoothOrchestrator @Inject constructor(@ApplicationContext val context: Context) {

    init {
        Log.v(TAG, "created")
    }

    private val btAdapter = BluetoothAdapter.getDefaultAdapter()

    val btDevices: MutableList<BluetoothDevice> = mutableListOf()
    private val btDeviceGattMap: MutableMap<String, BluetoothGatt> = mutableMapOf()
    val handler = Handler(context.mainLooper)

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

    fun startScanning() {
        btAdapter.bluetoothLeScanner.startScan(filter, scanSettings, scanCallback)
        handler.postDelayed({
            cancelScanning()
        }, 10000L)
    }

    fun cancelScanning() {
        handler.removeCallbacksAndMessages(null)
        btAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    fun connectAndroidWay(device: BluetoothDevice, listener: BluetoothEstablishListener) {
        Log.v(TAG, "connect on the android way: ${device.address}")
        device.connectGatt(context, false, AndroidGattCallback(listener))
    }

    fun connectWithLibrary(device: BluetoothDevice) {
        Log.v(TAG, "connect with the library: ${device.address}")
    }

    private inner class AndroidGattCallback(private val listener: BluetoothEstablishListener) :
        BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.v(TAG, "service discovered for $gatt")
            gatt?.services?.forEach {
                Log.v(TAG,
                    it.characteristics.joinToString(
                        separator = ",",
                    ) { it.uuid.toString() }
                )
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    btDeviceGattMap[deviceAddress] = gatt
                    listener.onConnected(deviceAddress)
                    gatt.discoverServices()

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    btDeviceGattMap.remove(deviceAddress)
                    listener.onDisconnected(deviceAddress)
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                listener.onError(deviceAddress, status.toString())
                gatt.close()
            }
        }
    }

    interface BluetoothEstablishListener {
        fun onConnected(macAddress: String)
        fun onDisconnected(macAddress: String)
        fun onError(macAddress: String, error: String)
    }
}