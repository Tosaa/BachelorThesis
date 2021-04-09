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
import asaa.bachelor.bleconnector.bt.custom.le.CustomLowEnergyDevice
import asaa.bachelor.bleconnector.bt.custom.le.NotificationStatus
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
    var lowEnergyDevice: CustomLowEnergyDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        lowEnergyDevice = bluetoothManager.btDevices.find { it.address == macAddress }?.let { CustomLowEnergyDevice(it) }
        lowEnergyDevice?.let { viewModel.bluetoothDevice.postValue(it.device) }
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
        lowEnergyDevice?.addGeneralObserver(this)
        viewModel.isIndicateActive.postValue(lowEnergyDevice?.indicateStatus == NotificationStatus.DONE(true))
        viewModel.isNotifyActive.postValue(lowEnergyDevice?.notifyStatus == NotificationStatus.DONE(true))
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        viewModel.connectionState.postValue(newStatus)
        if (newStatus is ConnectionStatus.DISCONNECTED) {
            // reset Fragment
            viewModel.isNotifyActive.postValue(false)
            viewModel.isIndicateActive.postValue(false)
            if (newStatus.reason.isNotEmpty()) {
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

    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
        lowEnergyDevice?.removeGeneralObserver(this)
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

        // Custom Service
        binding.customStatus.readButton.setOnClickListener {
            Timber.i("$macAddress: onClick: read ${CustomService.CUSTOM_SERVICE_1},${CustomCharacteristic.READ_CHARACTERISTIC}")
            lowEnergyDevice?.readCharacteristic1()
            lowEnergyDevice?.readCharacteristic2()
        }
        binding.customStatus.notifyButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Notify")
            lowEnergyDevice?.let { conn ->
                val isNotifyActive = viewModel.isNotifyActive.value ?: false

                if (isNotifyActive) {
                    Timber.v("stop Notify")
                    lowEnergyDevice?.stopNotify()
                } else {
                    Timber.v("start Notify")
                    lowEnergyDevice?.startNotify()
                }
            }
        }
        binding.customStatus.indicateButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Indicate ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.INDICATE_CHARACTERISTIC}")
            lowEnergyDevice?.let { conn ->
                val isIndicateActive = viewModel.isIndicateActive.value ?: false
                if (isIndicateActive) {
                    Timber.v("stop Indicate")
                    lowEnergyDevice?.stopIndicate()
                } else {
                    Timber.v("start Indicate")
                    lowEnergyDevice?.startIndicate()
                }
            }
        }
        binding.customStatus.writeButton.setOnClickListener {
            val text = binding.customStatus.writeTextField.text.toString()
            Timber.i("$macAddress: onClick: write $text")
            lowEnergyDevice?.write(text)
            binding.customStatus.writeTextField.setText("")
        }
        binding.customStatus.writeNoResponseButton.setOnClickListener {
            val text = binding.customStatus.writeTextField.text.toString()
            Timber.i("$macAddress: onClick: write without response $text")
            lowEnergyDevice?.writeWithoutResponse(text)
            binding.customStatus.writeTextField.setText("")
        }
    }
}