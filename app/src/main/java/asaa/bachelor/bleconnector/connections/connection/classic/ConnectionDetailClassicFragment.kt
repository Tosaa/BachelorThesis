package asaa.bachelor.bleconnector.connections.connection.classic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import asaa.bachelor.bleconnector.bt.custom.classic.BluetoothClassicDevice
import asaa.bachelor.bleconnector.bt.custom.classic.CustomClassicDevice
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.databinding.ConnectionDetailClassicFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailClassicFragment : Fragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    lateinit var binding: ConnectionDetailClassicFragmentBinding
    val viewModel: ConnectionDetailClassicViewModel by viewModels()
    val args: ConnectionDetailClassicFragmentArgs by navArgs()

    lateinit var macAddress: String
    private lateinit var btDevice: BluetoothDevice
    private var deviceBluetooth: BluetoothClassicDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailClassicFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        btDevice = bluetoothManager.btDevices.first { it.address == macAddress }
        viewModel.bluetoothDevice.postValue(btDevice)
        setupBinding()
        deviceBluetooth = CustomClassicDevice(btDevice)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        deviceBluetooth?.addObserver(viewModel)

    }

    private fun setupBinding() {
        binding.connectionState.stateButton.setOnClickListener {
            if (viewModel.connectionState.value == ConnectionStatus.CONNECTED)
                stopConnection()
            else
                startConnection()
        }
        binding.writeButton.setOnClickListener {
            deviceBluetooth?.write(lifecycleScope, "Test")
        }
    }

    private fun stopConnection() {
        Timber.i("${btDevice.address} on stop Connection clicked")
        deviceBluetooth?.disconnect()
    }

    private fun startConnection() {
        Timber.i("${btDevice.address} on start Connection clicked")
        deviceBluetooth?.connect(lifecycleScope)
    }

    override fun onStop() {
        deviceBluetooth?.removeObserver(viewModel)
        super.onStop()
    }
}