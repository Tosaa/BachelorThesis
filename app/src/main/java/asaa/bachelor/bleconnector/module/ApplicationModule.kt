package asaa.bachelor.bleconnector.module

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import asaa.bachelor.bleconnector.logging.FileLoggingTree
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(ActivityRetainedComponent::class)
object ApplicationModule {

    @Provides
    fun providePreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

}