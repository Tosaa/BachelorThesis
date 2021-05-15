package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.activityViewModels
import asaa.bachelor.bleconnector.bt.PhyLevel
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.ConnectionSettingsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionSettingsFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
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
        binding.connPhyUpdateButton.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                if (binding.connPhyInput.text.toString().toInt() == 1)
                    it.updatePhy(PhyLevel.LEVEL_1)
                else
                    it.updatePhy(PhyLevel.LEVEL_2)
            }
            binding.connPhyInput.setText("")
        }
        binding.characteristicSizeBtn.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach { connectionItem ->
                binding.characteristicSizeInput.text.toString().let {
                    if (it.isNotEmpty() && it.isDigitsOnly()) {
                        connectionItem.setValueSize(it.toInt())
                    }
                }

            }
        }

        return binding.root
    }
}