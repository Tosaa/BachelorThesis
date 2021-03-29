package asaa.bachelor.bleconnector.connections

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.work.impl.utils.ForceStopRunnable
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.databinding.ConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionsFragment : Fragment() {

    val viewModel: ConnectionsViewModel by viewModels()
    private lateinit var binding: ConnectionsFragmentBinding

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionsFragmentBinding.inflate(layoutInflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
        setupBtDevicesAdapter(binding.btDevicesRecyclerView)
        binding.startDiscoveryButton.setOnClickListener {
            Timber.i("start Discovery")
            viewModel.startScanning()
        }
        binding.stopDiscoveryButton.setOnClickListener {
            Timber.i("stop Discovery")
            viewModel.stopScanning()
        }

        binding.simultanEspActions.setOnClickListener {
            findNavController().navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToSimultanConnectionFragment())
        }
        return binding.root
    }

    private fun setupBtDevicesAdapter(recyclerView: RecyclerView) {

        val adapter = BluetoothDeviceAdapter(bluetoothOrchestrator, findNavController())

        recyclerView.adapter = adapter
        viewModel.btDevicesSize.observe(viewLifecycleOwner) {
            Timber.v("update BtDevices: ${bluetoothOrchestrator.btDevices}")
            adapter.updateDevices(bluetoothOrchestrator.btDevices)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScanning()
    }

}