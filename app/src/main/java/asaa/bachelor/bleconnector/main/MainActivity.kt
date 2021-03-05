package asaa.bachelor.bleconnector.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import asaa.bachelor.bleconnector.R
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var bluetoothOrchestrator: BluetoothOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        checkSystemSettings()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.menuBar.setNavigationOnClickListener {
            navController.popBackStack(R.id.mainFragment, false)
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

    fun checkSystemSettings() {
        // isBTActive
        val isBtActive = BluetoothAdapter.getDefaultAdapter().isEnabled
        Log.v("MainActivity", "setup BT active status: $isBtActive")
        sharedPreferences.edit().putBoolean("bluetooth_active", isBtActive).apply()
        // isLocationActive
        val isLocationEnabled = LocationManagerCompat.isLocationEnabled(getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        Log.v("MainActivity", "setup Location active status: $isLocationEnabled")
        sharedPreferences.edit().putBoolean("location_active", isLocationEnabled).apply()
        // LocationPermission
        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Log.v("MainActivity", "setup Location permission granted: $isLocationPermissionGranted")
        sharedPreferences.edit().putBoolean("location_permission_granted", isLocationPermissionGranted).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothOrchestrator.disconnectAll()
    }
}