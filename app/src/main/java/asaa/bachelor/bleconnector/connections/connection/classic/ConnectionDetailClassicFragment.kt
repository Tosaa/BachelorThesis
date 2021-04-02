package asaa.bachelor.bleconnector.connections.connection.classic

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.BluetoothConnection
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.connections.connection.ConnectionDetailFragmentArgs
import asaa.bachelor.bleconnector.databinding.ConnectionDetailClassicFragmentBinding
import asaa.bachelor.bleconnector.databinding.ConnectionDetailFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailClassicFragment : Fragment() {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    lateinit var binding: ConnectionDetailClassicFragmentBinding
    val viewModel: ConnectionDetailClassicViewModel by viewModels()
    val args: ConnectionDetailFragmentArgs by navArgs()
    lateinit var macAddress: String
    private lateinit var btDevice: BluetoothDevice
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var dataExchangeService: ClassicDataExchangeService? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailClassicFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        btDevice = bluetoothOrchestrator.btDevices.first { it.address == macAddress }
        viewModel.bluetoothDevice.postValue(btDevice)
        setupBinding()
        return binding.root
    }

    private fun setupBinding() {
        binding.connectionState.stateButton.setOnClickListener {
            startConnection()
        }
        binding.writeButton.setOnClickListener {
            dataExchangeService?.write(lifecycleScope, "Test")
        }
    }

    private fun startConnection() {
        dataExchangeService = ClassicDataExchangeService(btDevice)
        dataExchangeService?.addObserver(viewModel)
        dataExchangeService?.connect(lifecycleScope)
    }

    override fun onStop() {
        dataExchangeService?.removeObserver(viewModel)
        super.onStop()
    }
}