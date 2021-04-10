package asaa.bachelor.bleconnector.connections.connection.multi.commands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.connections.connection.multi.SimultanConnectionViewModel
import asaa.bachelor.bleconnector.databinding.WriteFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WriteFragment : SelectedConnectionCommandFragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: WriteFragmentBinding
    override val title = "Write"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WriteFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        setupBindings()

        return binding.root
    }

    private fun setupBindings() {
        binding.writeButton.setOnClickListener {
            val text = binding.writeTextField.text.toString()
            viewModel.connections.forEach {
                it?.write(text)
            }
            binding.writeTextField.setText("")
        }
        binding.writeWithoutResponseButton.setOnClickListener {
            val text = binding.writeTextField.text.toString()
            viewModel.connections.forEach {
                it?.write(text, true)
            }
            binding.writeTextField.setText("")
        }
    }
}