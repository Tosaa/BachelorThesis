package asaa.bachelor.bleconnector.connections.connection.multi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.databinding.SimultanConnectionsFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SimultanConnectionFragment : Fragment() {

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    val viewModel: SimultanConnectionViewModel by activityViewModels()
    lateinit var binding: SimultanConnectionsFragmentBinding
    private lateinit var adapter: CommandsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SimultanConnectionsFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        adapter = CommandsAdapter(this)
        binding.interactionViewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.interactionViewPager) { tab, position ->
            tab.text = adapter.fragments[position].title
        }.attach()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateConnections()
        Timber.v("onResume")
    }


    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
    }
}