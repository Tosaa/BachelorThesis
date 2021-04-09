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
import asaa.bachelor.bleconnector.bt.custom.classic.BluetoothClassicDevice
import asaa.bachelor.bleconnector.bt.custom.classic.CustomClassicDevice
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.connections.connection.ConnectionDetailFragmentArgs
import asaa.bachelor.bleconnector.databinding.ConnectionDetailClassicFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailClassicFragment : Fragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    lateinit var binding: ConnectionDetailClassicFragmentBinding
    val viewModel: ConnectionDetailClassicViewModel by viewModels()
    val args: ConnectionDetailFragmentArgs by navArgs()
    lateinit var macAddress: String
    private lateinit var btDevice: BluetoothDevice
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
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
        return binding.root
    }

    private fun setupBinding() {
        binding.connectionState.stateButton.setOnClickListener {
            startConnection()
        }
        binding.writeButton.setOnClickListener {
            deviceBluetooth?.write(lifecycleScope, "Test")
        }
    }

    private fun startConnection() {
        deviceBluetooth = CustomClassicDevice(btDevice)
        deviceBluetooth?.addObserver(viewModel)
        deviceBluetooth?.connect(lifecycleScope)
    }

    override fun onStop() {
        deviceBluetooth?.removeObserver(viewModel)
        super.onStop()
    }
}