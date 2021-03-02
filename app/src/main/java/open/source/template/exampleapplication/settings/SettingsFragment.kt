package open.source.template.exampleapplication.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import open.source.template.exampleapplication.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}