package asaa.bachelor.bleconnector.about

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import asaa.bachelor.bleconnector.databinding.AboutFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class AboutFragment : Fragment() {

    val viewModel: AboutViewModel by viewModels()
    private lateinit var binding: AboutFragmentBinding

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AboutFragmentBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }


}