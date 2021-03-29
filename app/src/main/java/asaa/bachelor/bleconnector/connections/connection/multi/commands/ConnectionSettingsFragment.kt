package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.bt.common.CustomCharacteristic
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.ConnectFragmentBinding
import asaa.bachelor.bleconnector.databinding.ConnectionSettingsFragmentBinding
import asaa.bachelor.bleconnector.databinding.ReadFragmentBinding
import asaa.bachelor.bleconnector.databinding.SimultanConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionSettingsFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: ConnectionSettingsFragmentBinding
    override val title = "Settings"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionSettingsFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mtuRequestButton.setOnClickListener {
            binding.mtuTextInput.text.toString().let { mtu ->
                if (mtu.isNotEmpty() && mtu.isDigitsOnly()) {
                    viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                        it.requestMTU(mtu.toInt())
                    }
                }
            }
        }
        binding.connIntervalRequestButton.setOnClickListener {
            binding.connIntervalInput.text.toString().let { interval ->
                if (interval.isNotEmpty() && interval.isDigitsOnly()) {
                    viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                        it.writeConnectionInterval(interval)
                    }
                }
            }
        }
        return binding.root
    }
}