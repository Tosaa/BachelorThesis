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
import asaa.bachelor.bleconnector.databinding.ReadFragmentBinding
import asaa.bachelor.bleconnector.databinding.SimultanConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ReadFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: ReadFragmentBinding
    override val title = "Read"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ReadFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.readSelected1.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("read C1: $it")
                if (it.isReady)
                    it.readC1()
            }
        }
        binding.readSelected2.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("read C2: $it")
                if (it.isReady)
                    it.readC2()
            }
        }
        return binding.root
    }
}