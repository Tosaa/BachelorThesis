package asaa.bachelor.bleconnector.connections

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import asaa.bachelor.bleconnector.bt.BondState
import asaa.bachelor.bleconnector.bt.ConnectionStatus
import asaa.bachelor.bleconnector.bt.DeviceType
import asaa.bachelor.bleconnector.bt.custom.CustomBluetoothDevice
import asaa.bachelor.bleconnector.bt.custom.classic.CustomClassicDevice
import asaa.bachelor.bleconnector.bt.custom.le.BluetoothLowEnergyDevice
import asaa.bachelor.bleconnector.bt.manager.BluetoothManager
import asaa.bachelor.bleconnector.databinding.BluetoothDeviceItemBinding
import timber.log.Timber

private const val TAG = "BluetoothDeviceAdapter"

class BluetoothDeviceAdapter(
    private val bluetoothManager: BluetoothManager,
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
            val deviceAddress = device.deviceTag
            val deviceType = when {
                device is BluetoothLowEnergyDevice -> "LE"
                device is CustomClassicDevice -> "Classic"
                else -> DeviceType.get(device.device.type)?.name ?: "Unknown"
            }
            holder.bind(
                deviceAddress,
                device.device.name ?: "UNKNOWN DEVICENAME",
                BondState.get(device.device.bondState)?.toString() ?: "UNKNOWN BONDSTATE",
                deviceType,
                position == expandedItem,
                bluetoothDevice.isConnected
            )
            holder.buttonA.setOnClickListener {
                Timber.v("$deviceAddress: on LE clicked")
                navController.navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToConnectionDetail(deviceAddress))
            }
            holder.buttonShowConnection.setOnClickListener {
                Timber.v("$deviceAddress: on show connection clicked")
                navController.navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToConnectionDetail(deviceAddress))
            }

            holder.buttonB.setOnClickListener {
                Timber.v("$deviceAddress: on Classic clicked")
                navController.navigate(ConnectionsFragmentDirections.actionConnectionsFragmentToConnectionDetailClassicFragment(deviceAddress))
            }
            holder.address.setOnClickListener {
                if (position == expandedItem) {
                    Timber.v("$deviceAddress: reduce Item")
                    expandedItem = -1
                } else {
                    Timber.v("$deviceAddress: expand Item")
                    expandedItem = position
                }
                notifyDataSetChanged()
            }
        }

    }

    override fun getItemCount(): Int {
        return btDevices.size
    }

    fun updateDevices(newBtDevices: List<CustomBluetoothDevice>) {
        btDevices.clear()
        btDevices.addAll(
            newBtDevices
                .map { device ->
                    BluetoothDeviceWithState(
                        device,
                        device.connectionStatus == ConnectionStatus.CONNECTED
                    )
                })
        Timber.v("update all Bluetooth Devices in Adapter: $btDevices")
        notifyDataSetChanged()
    }

    data class BluetoothDeviceWithState(val device: CustomBluetoothDevice, val isConnected: Boolean)
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
        binding.executePendingBindings()
    }
}