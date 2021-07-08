package asaa.bachelor.bleconnector.connections.connection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.bt.common.CustomService
import asaa.bachelor.bleconnector.bt.custom.le.ESP32Device
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.databinding.Esp32FragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ESP32Fragment : Fragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: ConnectionDetailViewModel by viewModels()
    lateinit var binding: Esp32FragmentBinding
    val args: ESP32FragmentArgs by navArgs()
    lateinit var macAddress: String
    var leDevice: ESP32Device? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = Esp32FragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        leDevice = bluetoothManager.getCustomBluetoothDeviceFor(macAddress) as ESP32Device
        viewModel.bluetoothDevice.postValue(leDevice)
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
        leDevice?.addGeneralObserver(viewModel)
        leDevice?.addObserver(viewModel)
    }

    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
        leDevice?.removeGeneralObserver(viewModel)
        leDevice?.removeObserver(viewModel)
    }

    private fun setupBinding() {
        // CONNECTION STATE
        binding.connectionState.stateButton.setOnClickListener {
            if (leDevice?.connectionStatus != viewModel.connectionState.value) {
                Timber.w("ConnectionState of Viewmodel is not same as real ConnectionState: ${viewModel.connectionState.value} != ${leDevice?.connectionStatus}")
            }
            if (viewModel.isConnected.value == true) {
                Timber.i("$macAddress: onClick: Disconnect")
                leDevice?.disconnect()
            } else {
                Timber.i("$macAddress: onClick: Connect")
                leDevice?.connect(requireContext(), false)
            }
        }
        binding.bondingState.stateButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Create bond")
            leDevice?.device?.createBond()
        }
        binding.discoveryState.stateButton.setOnClickListener {
            if (viewModel.isDiscovered.value == true) {
                viewModel.discoverState.value.let { discoveryState ->
                    if (discoveryState is DiscoveryStatus.DISCOVERED) {
                        Timber.i("$macAddress: onClick: Show Device Info")
                        discoveryState.services.joinToString(separator = "\n\n") {
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
                leDevice?.discoverServices()
            }
        }

        // Custom Service
        binding.customStatus.readButton.setOnClickListener {
            Timber.i("$macAddress: onClick: read characteristic")
            leDevice?.readCharacteristic1()
            // leDevice?.readCharacteristic2()
        }
        binding.customStatus.notifyButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Notify")
            leDevice?.let { device ->
                val isNotifyActive = viewModel.isNotifyActive.value ?: false

                if (isNotifyActive) {
                    Timber.v("stop Notify")
                    device.stopNotify()
                } else {
                    Timber.v("start Notify")
                    device.startNotify()
                }
            }
        }
        binding.customStatus.indicateButton.setOnClickListener {
            Timber.i("$macAddress: onClick: Toggle Indicate ${CustomService.CUSTOM_SERVICE_1}, ${CustomCharacteristic.INDICATE_CHARACTERISTIC}")
            leDevice?.let { conn ->
                val isIndicateActive = viewModel.isIndicateActive.value ?: false
                if (isIndicateActive) {
                    Timber.v("stop Indicate")
                    leDevice?.stopIndicate()
                } else {
                    Timber.v("start Indicate")
                    leDevice?.startIndicate()
                }
            }
        }
    }
}