package asaa.bachelor.bleconnector.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import asaa.bachelor.bleconnector.R
import dagger.hilt.android.AndroidEntryPoint
import asaa.bachelor.bleconnector.databinding.MainFragmentBinding

@AndroidEntryPoint
class MainFragment : Fragment() {


    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: MainFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        binding.btConnectionsButton.setOnClickListener {
            findNavController().navigate(R.id.connectionsFragment)
        }
        binding.saveLogsBtn.setOnClickListener {
            (requireActivity().application as ExampleApplication)?.saveLogs()
        }

        return binding.root
    }

}