package asaa.bachelor.bleconnector.connections.connection

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.compose.navArgument
import asaa.bachelor.bleconnector.databinding.DialogServicesCharacteristicsInfoBinding
import timber.log.Timber

class DeviceInfoFragment : DialogFragment() {
    lateinit var binding: DialogServicesCharacteristicsInfoBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogServicesCharacteristicsInfoBinding.inflate(inflater, container, false)
        binding.services = arguments?.getString(SERVICES)
        binding.servicesText.movementMethod = ScrollingMovementMethod()
        return binding.root
    }

    companion object {
        val SERVICES = "services_arg"

        fun newInstance(services: String): DeviceInfoFragment {
            val args = Bundle()
            args.putString(SERVICES, services)
            val fragment = DeviceInfoFragment()
            fragment.arguments = args
            return fragment
        }

    }
}