package asaa.bachelor.bleconnector.about

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(sharedPreferences: SharedPreferences) : ViewModel() {

    val preferencesExample =
        "Preference example: " + sharedPreferences.getBoolean("example", false).toString()
    val isBtActive = "Bluetooth active: " + BluetoothAdapter.getDefaultAdapter().isEnabled.toString()
    var isLocationPermissionGranted = "Location permission granted: " + false.toString()


}