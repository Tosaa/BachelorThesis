package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.ConnectFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: ConnectFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.connectButton.setOnClickListener { viewModel.connections.filterNotNull().filter { it.isSelected }.forEach { it.connect() } }
        binding.disconnectButton.setOnClickListener { viewModel.connections.filterNotNull().filter { it.isSelected }.forEach { it.disconnect() } }
        return binding.root
    }

    override val title = "Connect"
}