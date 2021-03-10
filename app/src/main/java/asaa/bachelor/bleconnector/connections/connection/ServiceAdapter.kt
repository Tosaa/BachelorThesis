package asaa.bachelor.bleconnector.connections.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import asaa.bachelor.bleconnector.bt.BluetoothCharacteristicProperty
import asaa.bachelor.bleconnector.bt.CommonCharacteristics
import asaa.bachelor.bleconnector.bt.CommonServices
import asaa.bachelor.bleconnector.databinding.ServiceItemBinding
import timber.log.Timber

class ServiceAdapter(private val clickHandler: ClickHandler) : RecyclerView.Adapter<ServiceAdapter.ServiceLayout>() {
    private var serviceAndCharacteristicViewModels = mutableMapOf<ServiceCharacteristicViewModel, String>()

    fun updateServices(list: List<BluetoothGattService>) {
        serviceAndCharacteristicViewModels.clear()
        list.forEach {
            it.characteristics.forEach {
                serviceAndCharacteristicViewModels[ServiceCharacteristicViewModel(it)] = "None"
            }
        }
        Timber.v("Services updated:$list -> $serviceAndCharacteristicViewModels")
    }

    fun readOn(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        val viewModel = serviceAndCharacteristicViewModels.keys.find { it.characteristic == characteristic } ?: return
        val position = serviceAndCharacteristicViewModels.keys.indexOf(viewModel)
        val decodedValue = value?.joinToString(" ") { it.toChar().toString() }
        serviceAndCharacteristicViewModels[viewModel] = decodedValue
        Timber.v("update Item no:$position and value:$value,$decodedValue")

        Handler(Looper.getMainLooper()).post { notifyItemChanged(position) }
    }

    fun writeOn(characteristc: BluetoothGattCharacteristic) {}
    fun notifyOn(characteristc: BluetoothGattCharacteristic) {}
    fun indicateOn(characteristc: BluetoothGattCharacteristic) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceLayout {
        return ServiceLayout(ServiceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ServiceLayout, position: Int) {
        val viewModel = serviceAndCharacteristicViewModels.keys.toList()[position]
        holder.bind(viewModel, serviceAndCharacteristicViewModels[viewModel] ?: "")
    }


    override fun getItemCount(): Int {
        return serviceAndCharacteristicViewModels.size
    }

    inner class ServiceLayout(private val binding: ServiceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(serviceCharacteristic: ServiceCharacteristicViewModel, value: String) {
            binding.title.text = serviceCharacteristic.title

            if (serviceCharacteristic.hasWriteProperty) {
                binding.writeProperty.visibility = View.VISIBLE
                binding.writeButton.setOnClickListener { serviceCharacteristic.characteristic?.let { clickHandler.onWriteClick(it, binding.writeText.text.toString()) } }
                binding.writeText.setText(value)
            }
            if (serviceCharacteristic.hasReadProperty) {
                binding.readProperty.visibility = View.VISIBLE
                binding.readButton.setOnClickListener { serviceCharacteristic.characteristic?.let { clickHandler.onReadClick(it) } }
                binding.readText.text = value
            }
            if (serviceCharacteristic.hasNotifyProperty) {
                binding.notifyProperty.visibility = View.VISIBLE
                binding.notifyButton.setOnClickListener { serviceCharacteristic.characteristic?.let { clickHandler.onNotifyClick(it) } }
                binding.notifyText.text = value
            }
            if (serviceCharacteristic.hasIndicateProperty) {
                binding.indicateProperty.visibility = View.VISIBLE
                binding.indicateButton.setOnClickListener { serviceCharacteristic.characteristic?.let { clickHandler.onIndicateClick(it) } }
                binding.indicateText.text = value
            }
        }
    }

    inner class ServiceCharacteristicViewModel(characteristic: BluetoothGattCharacteristic) {

        var characteristic: BluetoothGattCharacteristic? = characteristic
        var characteristicUUID = "No UUID"
        var serviceUUID = "No UUID"
        var title = "No Title"

        var hasWriteProperty = false
        var hasReadProperty = false
        var hasIndicateProperty = false
        var hasNotifyProperty = false

        var writeValue = "none"
        var readValue = "none"
        var indicateValue = "none"
        var notifyValue = "none"

        init {
            val properties = BluetoothCharacteristicProperty.transform(characteristic.properties)
            hasWriteProperty = BluetoothCharacteristicProperty.WRITE in properties
            hasReadProperty = BluetoothCharacteristicProperty.READ in properties
            hasIndicateProperty = BluetoothCharacteristicProperty.INDICATE in properties
            hasNotifyProperty = BluetoothCharacteristicProperty.NOTIFY in properties
            val characteristicValue = characteristic.value?.joinToString(separator = "") { it.toChar().toString() } ?: "None"
            if (hasReadProperty)
                readValue = characteristicValue
            if (hasNotifyProperty)
                notifyValue = characteristicValue
            if (hasIndicateProperty)
                indicateValue = characteristicValue
            if (hasWriteProperty)
                writeValue = characteristicValue
            characteristicUUID = CommonCharacteristics.mapIfExists(characteristic.uuid.toString())?.toString() ?: characteristic.uuid.toString()
            serviceUUID = CommonServices.mapIfExists(characteristic.service.uuid.toString())?.toString() ?: characteristic.service.uuid.toString()
            title = "$serviceUUID - $characteristicUUID"
        }

    }
}


interface ClickHandler {
    fun onReadClick(characteristc: BluetoothGattCharacteristic)
    fun onWriteClick(characteristc: BluetoothGattCharacteristic, value: String)
    fun onNotifyClick(characteristc: BluetoothGattCharacteristic)
    fun onIndicateClick(characteristc: BluetoothGattCharacteristic)
}