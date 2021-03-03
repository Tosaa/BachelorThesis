package asaa.bachelor.bleconnector.connections

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.bt.BtUtil
import asaa.bachelor.bleconnector.databinding.BluetoothDeviceItemBinding

class BluetoothDeviceAdapter(val bluetoothOrchestrator: BluetoothOrchestrator) :
    RecyclerView.Adapter<DeviceViewHolder>() {
    private val btDevices = mutableListOf<BluetoothDevice>()
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
        btDevices[position].let {
            holder.bind(
                it.address,
                it.name ?: "UNKNOWN",
                BtUtil.resolveBond(it.bondState),
                BtUtil.resolveDeviceType(it.type),
                position == expandedItem
            )
            holder.buttonA.setOnClickListener {
                bluetoothOrchestrator.connectAndroidWay(btDevices[position],
                    object : BluetoothOrchestrator.BluetoothEstablishListener {
                        override fun onConnected(macAddress: String) {
                            Log.v("BluetoothDeviceAdapter", "connected: $macAddress")
                        }

                        override fun onDisconnected(macAddress: String) {
                            Log.v("BluetoothDeviceAdapter", "disconnected: $macAddress")
                        }

                        override fun onError(macAddress: String, error: String) {
                            Log.v("BluetoothDeviceAdapter", "error ($error): $macAddress")
                        }

                    })
            }
            holder.buttonB.setOnClickListener {
                bluetoothOrchestrator.connectWithLibrary(btDevices[position])
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
        btDevices.addAll(newBtDevices)
        notifyDataSetChanged()
    }
}

class DeviceViewHolder(private val binding: BluetoothDeviceItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val address = binding.deviceAddress
    val buttonA = binding.connectButtonA
    val buttonB = binding.connectButtonB

    fun bind(
        deviceAddress: String,
        deviceName: String,
        bondState: String,
        deviceType: String,
        isExpanded: Boolean
    ) {
        binding.deviceAddress.text = deviceAddress
        binding.deviceName.text = deviceName
        binding.deviceBondingState.text = bondState
        binding.deviceType.text = deviceType
        binding.connectButtons.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }
}