package asaa.bachelor.bleconnector.connections.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CommonCharacteristics
import asaa.bachelor.bleconnector.bt.common.CommonServices
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.bt.common.CustomService
import asaa.bachelor.bleconnector.bt.custom.le.BluetoothLowEnergyDevice
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.databinding.ConnectionDetailFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailFragment : Fragment(), IStatusObserver {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: ConnectionDetailViewModel by viewModels()
    lateinit var binding: ConnectionDetailFragmentBinding
    val args: ConnectionDetailFragmentArgs by navArgs()
    lateinit var macAddress: String
    var lowEnergyDevice: BluetoothLowEnergyDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        lowEnergyDevice = bluetoothManager.connectionFor(macAddress)
        lowEnergyDevice?.let { viewModel.bluetoothDevice.postValue(it.device) }
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
        lowEnergyDevice?.addObserver(this)
        viewModel.isIndicateActive.postValue(lowEnergyDevice?.isIndicateActive(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.INDICATE_CHARACTERISTIC.uuid))
        viewModel.isNotifyActive.postValue(lowEnergyDevice?.isNotifyActive(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.NOTIFY_CHARACTERISTIC.uuid))
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        Timber.v("$macAddress: onConnectionStateChanged ${viewModel.connectionState.value} - $newStatus")
        viewModel.connectionState.postValue(newStatus)
        if (newStatus is ConnectionStatus.DISCONNECTED) {
            // reset Fragment
            viewModel.isNotifyActive.postValue(false)
            viewModel.isIndicateActive.postValue(false)
            if (newStatus.reason.length > 0) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(requireContext(), "could not connect because of ${newStatus.reason}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        Timber.v("$macAddress: onDiscoveryStateChanged ${viewModel.discoverState.value} - $newDiscoveryState")
        viewModel.discoverState.postValue(newDiscoveryState)
    }

    override fun onBondStateChanged(bond: BondState) {
        super.onBondStateChanged(bond)
        Timber.v("$macAddress: onBondStateChanged ${viewModel.bondState.value} - $bond")
        viewModel.bondState.postValue(bond)
    }

    override fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        super.onWriteCharacteristic(characteristic, value, status)
        val writtenValue = value.joinToString(separator = "") { it.toChar().toString() }
        val characteristicMatch = CommonCharacteristics.mapIfExists(characteristic.uuid.toString()) ?: CustomCharacteristic.mapIfExists(characteristic.uuid.toString())
        Timber.v("$macAddress: onWriteCharacteristic: received: $writtenValue for $characteristicMatch")

        when (status) {
            BluetoothGattStatus.GATT_SUCCESS -> Handler(Looper.getMainLooper()).post { Toast.makeText(requireContext(), "wrote: $writtenValue", Toast.LENGTH_SHORT).show() }
            BluetoothGattStatus.GATT_WRITE_NOT_PERMITTED -> {
                Timber.w("Insufficient Permission to write: $characteristic")
                return
            }
            else -> {
                Timber.w("Error on Reading characteristic: $characteristic")
                return
            }
        }
    }

    override fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onReadCharacteristic(characteristic, value)
        val uuid = characteristic.uuid.toString()
        val readValue = value.joinToString(separator = "") { it.toChar().toString() }
        val characteristicMatch = CommonCharacteristics.mapIfExists(uuid) ?: CustomCharacteristic.mapIfExists(uuid)
        Timber.v("$macAddress: onReadCharacteristic: received: $readValue for $characteristicMatch")
        when (characteristicMatch) {
            CommonCharacteristics.BatteryLevel -> viewModel.batteryValue.postValue(readValue)
            CustomCharacteristic.READ_CHARACTERISTIC -> viewModel.customReadValue.postValue(readValue)
            CustomCharacteristic.READ_CHARACTERISTIC_2 -> viewModel.customReadValue.postValue(readValue)
            CustomCharacteristic.INDICATE_CHARACTERISTIC -> viewModel.customIndicateValue.postValue(readValue)
            CustomCharacteristic.NOTIFY_CHARACTERISTIC -> viewModel.customNotifyValue.postValue(readValue)
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
        lowEnergyDevice?.removeObserver(this)
    }

    private fun setupBinding() {
        // CONNECTION STATE
        binding.connectionState.stateButton.setOnClickListener {
            if (lowEnergyDevice?.connectionStatus != viewModel.connectionState.value) {
                Timber.w("ConnectionState of Viewmodel is not same as real ConnectionState: ${viewModel.connectionState.value} != ${lowEnergyDevice?.connectionStatus}")
            }
            if (viewModel.isConnected.value == true) {
                Timber.i("$macAddress: onClick: Disconnect")
                lowEnergyDevice?.disconnect()
            } else {
                Timber.i("$macAddress: onClick: Connect")
                lowEnergyDevice?.connect(requireContext(), false)
            }
        }
        binding.bondingState.stateButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Create bond")
            lowEnergyDevice?.device?.createBond()
        }
        binding.discoveryState.stateButton.setOnClickListener {
            if (viewModel.isDiscovered.value == true) {
                viewModel.discoverState.value.let { discoveryState ->
                    if (discoveryState is DiscoveryStatus.DISCOVERED) {
                        Timber.i("$macAddress: onClick: Show Device Info")
                        discoveryState?.services.joinToString(separator = "\n\n") {
                            BtUtil.serviceToString(it.uuid.toString()) + "\n" +
                                    it.characteristics.joinToString("\n-", prefix = "-") {
                                        BtUtil.characteristicToString(it.uuid.toString())
                                    }
                        }.let {
                            DeviceInfoFragment.newInstance(it).show(parentFragmentManager, "service_characteristic_dialog")
                        }

                    }
                }
            } else {
                Timber.i("$macAddress: onClick: Discover")
                lowEnergyDevice?.discoverServices()
            }
        }
        // BATTERY
        binding.batteryStatus.readButton.setOnClickListener {
            Timber.i("$macAddress: onClick: read Battery")
            lowEnergyDevice?.requestRead(CommonServices.Battery.longUUID, CommonCharacteristics.BatteryLevel.longUUID)
        }
        // Custom Service
        binding.customStatus.readButton.setOnClickListener {
            Timber.i("$macAddress: onClick: read ${CustomService.CUSTOM_SERVICE_1},${CustomCharacteristic.READ_CHARACTERISTIC}")
            lowEnergyDevice?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC.uuid)
            lowEnergyDevice?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC_2.uuid)
        }
        binding.customStatus.notifyButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Notify ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.NOTIFY_CHARACTERISTIC}")
            lowEnergyDevice?.let { conn ->
                val isNotifyActive = viewModel.isNotifyActive.value ?: false

                if (isNotifyActive) {
                    Timber.v("stop Notify")
                    !conn.requestStopNotifyOrIndicate(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.NOTIFY_CHARACTERISTIC.uuid)
                } else {
                    Timber.v("start Notify")
                    conn.requestStartNotify(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.NOTIFY_CHARACTERISTIC.uuid)
                }.let { isActive ->
                    viewModel.isNotifyActive.postValue(isActive)
                }

            }
        }
        binding.customStatus.indicateButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Indicate ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.INDICATE_CHARACTERISTIC}")
            lowEnergyDevice?.let { conn ->
                val isIndicateActive = viewModel.isIndicateActive.value ?: false
                if (isIndicateActive) {
                    Timber.v("stop Indicate")
                    !conn.requestStopNotifyOrIndicate(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.INDICATE_CHARACTERISTIC.uuid)
                } else {
                    Timber.v("start Indicate")
                    conn.requestStartIndicate(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.INDICATE_CHARACTERISTIC.uuid)
                }.let { isActive ->
                    viewModel.isIndicateActive.postValue(isActive)
                }
            }
        }
        binding.customStatus.writeButton.setOnClickListener {
            val text = binding.customStatus.writeTextField.text.toString()
            Timber.i("$macAddress: onClick: Write ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.WRITE_CHARACTERISTIC}, $text")
            lowEnergyDevice?.requestWrite(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.WRITE_CHARACTERISTIC.uuid, text)
            binding.customStatus.writeTextField.setText("")
        }
        binding.customStatus.writeNoResponseButton.setOnClickListener {
            val text = binding.customStatus.writeTextField.text.toString()
            Timber.i("$macAddress: onClick: Write ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.WRITE_WO_RESPONSE_CHARACTERISTIC}, $text")
            lowEnergyDevice?.requestWrite(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.WRITE_WO_RESPONSE_CHARACTERISTIC.uuid, text)
            binding.customStatus.writeTextField.setText("")
        }
    }
}