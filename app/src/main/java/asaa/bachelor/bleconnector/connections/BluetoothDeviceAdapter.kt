package asaa.bachelor.bleconnector.connections

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import asaa.bachelor.bleconnector.bt.*
import asaa.bachelor.bleconnector.databinding.BluetoothDeviceItemBinding
import timber.log.Timber

private const val TAG = "BluetoothDeviceAdapter"

class BluetoothDeviceAdapter(
    private val bluetoothOrchestrator: BluetoothOrchestrator,
    private val navController: NavController
) :
    RecyclerView.Adapter<DeviceViewHolder>() {
    private val btDevices = mutableListOf<BluetoothDeviceWithState>()
    private var expandedItem = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            BluetoothDeviceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        btDevices[position].let { bluetoothDevice ->
            val device = bluetoothDevice.device
            holder.bind(
                device.address,
                device.name ?: "UNKNOWN DEVICENAME",
                BondState.get(device.bondState)?.toString() ?: "UNKNOWN BONDSTATE",
                DeviceType.get(device.type)?.toString() ?: "UNKNOWN DEVICETYPE",
                position == expandedItem,
                bluetoothDevice.isConnected
            )
            holder.buttonA.setOnClickListener {
                Timber.v("on A clicked")
                navController.navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToConnectionDetail(btDevices[position].device.address))
            }
            holder.buttonShowConnection.setOnClickListener {
                Timber.v("on show connection clicked")
                navController.navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToConnectionDetail(btDevices[position].device.address))
            }

            holder.buttonB.setOnClickListener {
                // bluetoothOrchestrator.connectWithLibrary(btDevices[position])
            }
            holder.address.setOnClickListener {
                if (position == expandedItem) {
                    expandedItem = -1
                } else {
                    expandedItem = position
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return btDevices.size
    }

    fun updateDevices(newBtDevices: List<BluetoothDevice>) {
        btDevices.clear()
        btDevices.addAll(newBtDevices.map { BluetoothDeviceWithState(it, bluetoothOrchestrator.connectionFor(it.address)?.connectionStatus.let { it == ConnectionStatus.CONNECTED }) })
        Timber.v("update all BluetoothDevices: $btDevices")
        notifyDataSetChanged()
    }

    data class BluetoothDeviceWithState(val device: BluetoothDevice, val isConnected: Boolean)
}

class DeviceViewHolder(private val binding: BluetoothDeviceItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val address = binding.deviceAddress
    val buttonA = binding.connectButtonA
    val buttonB = binding.connectButtonB
    val buttonShowConnection = binding.buttonShowConnection
    fun bind(
        deviceAddress: String,
        deviceName: String,
        bondState: String,
        deviceType: String,
        isExpanded: Boolean,
        isConnected: Boolean
    ) {
        binding.deviceAddress.text = deviceAddress
        binding.deviceName.text = deviceName
        binding.deviceBondingState.text = bondState
        binding.deviceType.text = deviceType
        binding.connectButtons.visibility = if (isExpanded && !isConnected) View.VISIBLE else View.GONE
        binding.buttonShowConnection.visibility = if (isExpanded && isConnected) View.VISIBLE else View.GONE
    }
}