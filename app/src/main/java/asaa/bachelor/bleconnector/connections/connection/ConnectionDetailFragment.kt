package asaa.bachelor.bleconnector.connections.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.databinding.ConnectionDetailFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ConnectionDetailFragment : Fragment(), IStatusObserver {

    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator
    val viewModel: ConnectionDetailViewModel by viewModels()
    lateinit var binding: ConnectionDetailFragmentBinding
    val args: ConnectionDetailFragmentArgs by navArgs()
    lateinit var macAddress: String
    var connection: BluetoothConnection? = null
    private val clickHandler: ClickHandler = object : ClickHandler {
        override fun onReadClick(characteristic: BluetoothGattCharacteristic) {
            Timber.v("onRead:${characteristic.toString()}")
            connection?.readCharacteristic(characteristic)
        }

        override fun onWriteClick(characteristc: BluetoothGattCharacteristic, value: String) {
            Timber.v("onWrite:${characteristc.toString()},$value")
        }

        override fun onNotifyClick(characteristc: BluetoothGattCharacteristic) {
            Timber.v("onNotify:${characteristc.toString()}")
        }

        override fun onIndicateClick(characteristc: BluetoothGattCharacteristic) {
            Timber.v("onIndicate:${characteristc.toString()}")
        }

    }
    val adapter = ServiceAdapter(clickHandler)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        macAddress = args.macAddress
        connection = bluetoothOrchestrator.connectionFor(macAddress)
        connection?.let { viewModel.bluetoothDevice.postValue(it.device) }
        setupBinding()
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Timber.v("onResume")
        connection?.addObserver(this)
    }

    override fun onConnectionStateChanged(newStatus: ConnectionStatus) {
        super.onConnectionStateChanged(newStatus)
        viewModel.connectionState.postValue(newStatus)
        if (newStatus is ConnectionStatus.DISCONNECTED) {
            if (newStatus.reason.isNotEmpty())
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(requireContext(), "could not connect because of ${newStatus.reason}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDiscoveryStateChanged(newDiscoveryState: DiscoveryStatus) {
        super.onDiscoveryStateChanged(newDiscoveryState)
        viewModel.discoverState.postValue(newDiscoveryState)
        if (newDiscoveryState is DiscoveryStatus.DISCOVERED) {
            adapter.updateServices(newDiscoveryState.services)
            Handler(Looper.getMainLooper()).post {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onBondStateChanged(bond: BondState) {
        super.onBondStateChanged(bond)
        viewModel.bondState.postValue(bond)
    }

    override fun onReadCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: BluetoothGattStatus) {
        super.onReadCharacteristic(characteristic, value, status)
        adapter.readOn(characteristic, value)
    }

    override fun onPause() {
        super.onPause()
        Timber.v("onPause")
        connection?.removeObserver(this)
    }

    private fun setupBinding() {
        binding.readBatteryStatusButton.setOnClickListener {
            connection?.readCharacteristic(CommonServices.Battery.longUUID, CommonCharacteristics.BatteryLevel.longUUID)
        }
        binding.connectionState.stateButton.setOnClickListener {
            if (connection?.connectionStatus != viewModel.connectionState.value) {
                Timber.w("ConnectionState of Viewmodel is not same as real ConnectionState: ${viewModel.connectionState.value} != ${connection?.connectionStatus}")
            }
            if (viewModel.isConnected.value == true) {
                connection?.disconnect()
            } else {
                connection?.connect(requireContext(), false)
            }
        }
        binding.bondingState.stateButton.setOnClickListener {
            connection?.device?.createBond()
        }
        binding.discoveryState.stateButton.setOnClickListener {
            connection?.discoverServices()
        }
        binding.serviceCharacteristicRecylcerView.adapter = adapter
    }
}