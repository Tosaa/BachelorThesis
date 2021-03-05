package asaa.bachelor.bleconnector.connections.connection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.databinding.ConnectionDetailFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
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
        viewModel.macAddress.postValue(macAddress)
        connection = bluetoothOrchestrator.connectionFor(macAddress)
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Log.v("TAG", "onResume")
        connection?.addObserver(this)
    }

    override fun onStatusChanged(newStatus: ConnectionStatus) {
        super.onStatusChanged(newStatus)
        viewModel.connectionState.postValue(newStatus::class.java.simpleName)
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        when (newDiscoveryState) {
            DiscoveryStatus.DISCOVERY_STARTED -> viewModel.services.postValue(emptyList())
            is DiscoveryStatus.DISCOVERY_FINISHED -> {
                viewModel.services.postValue(newDiscoveryState.services)
            }
            is DiscoveryStatus.DISCOVERY_FAILED -> {
            }
        }
    }


    override fun onPause() {
        super.onPause()
        Log.v("TAG", "onPause")
        connection?.removeObserver(this)
    }

    private fun setupBinding() {
        binding.readBatteryStatusButton.setOnClickListener {
            connection?.readCharacteristic(BtUtil.CommonServices.BatteryService.uuid, BtUtil.CommonCharacteristics.BatteryCharacteristic.uuid)
        }
    }
}