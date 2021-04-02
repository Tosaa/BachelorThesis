package asaa.bachelor.bleconnector.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import asaa.bachelor.bleconnector.R
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
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

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Timber.i("found new Bt Device: ${device?.name} , ${device?.address}")

                    device?.let {
                        bluetoothOrchestrator.addBluetoothDevice(it)
                    }
                    Handler(Looper.getMainLooper()).run {
                        Toast.makeText(context, "Found BT Device: ${device?.address}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun checkSystemSettings() {
        // isBTActive
        val isBtActive = BluetoothAdapter.getDefaultAdapter().isEnabled
        Timber.v("setup BT active status: $isBtActive")
        sharedPreferences.edit().putBoolean("bluetooth_active", isBtActive).apply()
        // isLocationActive
        val isLocationEnabled = LocationManagerCompat.isLocationEnabled(getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        Timber.v("setup Location active status: $isLocationEnabled")
        sharedPreferences.edit().putBoolean("location_active", isLocationEnabled).apply()
        // LocationPermission
        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Timber.v("setup Location permission granted: $isLocationPermissionGranted")
        sharedPreferences.edit().putBoolean("location_permission_granted", isLocationPermissionGranted).apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        (application as ExampleApplication).saveLogs()
        bluetoothOrchestrator.disconnectAll()
    }


    override fun onStop() {
        super.onStop()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

}