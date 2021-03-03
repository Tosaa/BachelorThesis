package asaa.bachelor.bleconnector.module

import android.content.Context
import asaa.bachelor.bleconnector.bt.BluetoothOrchestrator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    /*
    @Provides
    fun providesBluetoothOrchestrator(
        @ApplicationContext context: Context
    ): BluetoothOrchestrator {
        return BluetoothOrchestrator(context)
    }

     */
}