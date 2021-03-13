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
import asaa.bachelor.bleconnector.databinding.ConnectionDetailFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailFragment : Fragment(), IStatusObserver {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    val viewModel: ConnectionDetailViewModel by viewModels()
    lateinit var binding: ConnectionDetailFragmentBinding
    val args: ConnectionDetailFragmentArgs by navArgs()
    lateinit var macAddress: String
    var connection: BluetoothConnection? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        connection = bluetoothOrchestrator.connectionFor(macAddress)
        connection?.let { viewModel.bluetoothDevice.postValue(it.device) }
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
        connection?.addObserver(this)
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        viewModel.connectionState.postValue(newStatus)
        if (newStatus is ConnectionStatus.DISCONNECTED) {
            // reset Fragment
            viewModel.isNotifyActive.postValue(false)
            viewModel.isIndicateActive.postValue(false)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireContext(), "could not connect because of ${newStatus.reason}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        viewModel.discoverState.postValue(newDiscoveryState)
    }

    override fun onBondStateChanged(bond: BondState) {
        super.onBondStateChanged(bond)
        viewModel.bondState.postValue(bond)
    }

    override fun onWriteCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        super.onWriteCharacteristic(characteristic, value, status)
        val writtenValue = value.joinToString(separator = "") { it.toChar().toString() }

        when (status) {
            BluetoothGattStatus.GATT_SUCCESS -> Handler(Looper.getMainLooper()).post { Toast.makeText(requireContext(), "wrote: $writtenValue", Toast.LENGTH_SHORT).show() }
            BluetoothGattStatus.GATT_WRITE_NOT_PERMITTED -> {
                Timber.w("Insufficient Permission to write:$characteristic")
                return
            }
            else -> {
                Timber.w("Error on Reading characteristic:$characteristic")
                return
            }
        }
    }

    override fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        super.onReadCharacteristic(characteristic, value)
        val uuid = characteristic.uuid.toString()
        val readValue = value.joinToString(separator = "") { it.toChar().toString() }
        val characteristicMatch = CommonCharacteristics.mapIfExists(uuid) ?: CustomCharacteristic.mapIfExists(uuid)
        Timber.v("onReadCharacteristic: received:$readValue for $characteristicMatch")
        when (characteristicMatch) {
            CommonCharacteristics.BatteryLevel -> viewModel.batteryValue.postValue(readValue)
            CustomCharacteristic.READ_CHARACTERISTIC -> viewModel.customReadValue.postValue(readValue)
            CustomCharacteristic.INDICATE_CHARACTERISTIC -> viewModel.customIndicateValue.postValue(readValue)
            CustomCharacteristic.NOTIFY_CHARACTERISTIC -> viewModel.customNotifyValue.postValue(readValue)
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
        connection?.removeObserver(this)
    }

    private fun setupBinding() {
        // CONNECTION STATE
        binding.connectionState.stateButton.setOnClickListener {
            if (connection?.connectionStatus != viewModel.connectionState.value) {
                Timber.w("ConnectionState of Viewmodel is not same as real ConnectionState: ${viewModel.connectionState.value} != ${connection?.connectionStatus}")
            }
            if (viewModel.isConnected.value == true) {
                connection?.disconnect()
            } else {
                connection?.connect(requireContext(), false)
            }
        }
        binding.bondingState.stateButton.setOnClickListener {
            connection?.device?.createBond()
        }
        binding.discoveryState.stateButton.setOnClickListener {
            if (viewModel.isDiscovered.value == true) {
                viewModel.discoverState.value.let { discoveryState ->
                    if (discoveryState is DiscoveryStatus.DISCOVERED) {
                        Timber.v("show DeviceInfoFragment")
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
                connection?.discoverServices()
            }
        }
        // BATTERY
        binding.batteryStatus.readButton.setOnClickListener {
            connection?.requestRead(CommonServices.Battery.longUUID, CommonCharacteristics.BatteryLevel.longUUID)
        }
        // Custom Service
        binding.customStatus.readButton.setOnClickListener {
            connection?.requestRead(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.READ_CHARACTERISTIC.uuid)
        }
        binding.customStatus.notifyButton.setOnClickListener {
            connection?.let { conn ->
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
            connection?.let { conn ->
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
            connection?.requestWrite(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.WRITE_CHARACTERISTIC.uuid, binding.customStatus.writeTextField.text.toString())
            binding.customStatus.writeTextField.setText("")
        }
        binding.customStatus.writeNoResponseButton.setOnClickListener {
            connection?.requestWrite(CustomService.CUSTOM_SERVICE_1.uuid, CustomCharacteristic.WRITE_WO_RESPONSE_CHARACTERISTIC.uuid, binding.customStatus.writeTextField.text.toString())
            binding.customStatus.writeTextField.setText("")
        }
    }
}