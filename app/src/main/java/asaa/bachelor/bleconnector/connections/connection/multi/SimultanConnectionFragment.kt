package asaa.bachelor.bleconnector.connections.connection.multi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.databinding.SimultanConnectionsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SimultanConnectionFragment : Fragment() {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    val viewModel: SimultanConnectionViewModel by viewModels()
    lateinit var binding: SimultanConnectionsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SimultanConnectionsFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        binding.connectSelected.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("connect: $it")
                it.connect()
            }
        }

        binding.disconnectSelected.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("disconnect: $it")
                it.disconnect()
            }
        }
        binding.readSelected.setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
    }


    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
    }
}