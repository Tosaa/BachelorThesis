package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.ConnectFragmentBinding
import asaa.bachelor.bleconnector.databinding.SimultanConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
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