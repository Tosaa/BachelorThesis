package asaa.bachelor.bleconnector.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.work.impl.utils.ForceStopRunnable
import asaa.bachelor.bleconnector.R
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.bt.BondState
import asaa.bachelor.bleconnector.bt.BtUtil
import asaa.bachelor.bleconnector.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.security.KeyStore
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

        // Register Bond State Listener
        Timber.v("register broadcast receiver for bond connections")
        listenToBondStateChanges(this)
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
        bluetoothOrchestrator.disconnectAll()
    }


    override fun onStop() {
        super.onStop()
        Timber.v("unregister broadcast receiver for bond connections")
        applicationContext.unregisterReceiver(broadcastReceiver)
    }

    fun listenToBondStateChanges(context: Context) {
        context.applicationContext.registerReceiver(
            broadcastReceiver,
            IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        )
    }

    private val broadcastReceiver = object : ForceStopRunnable.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val previousBondState = getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                    val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val bondTransition = "${BondState.get(previousBondState)} to ${BondState.get(bondState)}"
                    Timber.v("${device?.address} bond state changed | $bondTransition")
                }
            }
        }
    }

}