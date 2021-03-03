package asaa.bachelor.bleconnector.connections

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.databinding.ConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
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
        return binding.root
    }

    private fun setupBtDevicesAdapter(recyclerView: RecyclerView) {
        val adapter = BluetoothDeviceAdapter(bluetoothOrchestrator)
        recyclerView.adapter = adapter
        viewModel.btDevicesSize.observe(viewLifecycleOwner) {
            Log.v("ConnectionsFragment", "${bluetoothOrchestrator.btDevices}")
            adapter.updateDevices(bluetoothOrchestrator.btDevices)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScanning()
    }

}