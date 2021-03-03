package asaa.bachelor.bleconnector.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import asaa.bachelor.bleconnector.R
import asaa.bachelor.bleconnector.databinding.ActivityMainBinding
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.menuBar.setNavigationOnClickListener {
            navController.popBackStack(R.id.mainFragment,false)
        }
        binding.menuBar.setOnMenuItemClickListener {
            when (it.title.toString()) {
                "about" -> {
                    navController.popBackStack(R.id.mainFragment, false)
                    navController.navigate(R.id.aboutFragment)
                    true
                }
                "settings" -> {
                    navController.popBackStack(R.id.mainFragment, false)
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> {
                    false
                }
            }

        }
    }
}