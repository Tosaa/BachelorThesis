package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.NotifyIndicateFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotifyIndicateFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: NotifyIndicateFragmentBinding
    override val title = "Notify/Indicate"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NotifyIndicateFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        binding.notifyBtn.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("toggle notify: $it")
                if (it.isReady)
                    it.toggleNotify()
            }
        }
        binding.indicateBtn.setOnClickListener {
            viewModel.connections.filterNotNull().filter { it.isSelected }.forEach {
                Timber.d("toggle indicate: $it")
                if (it.isReady)
                    it.toggleIndicate()
            }
        }
        return binding.root
    }
}