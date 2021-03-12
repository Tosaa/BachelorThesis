package asaa.bachelor.bleconnector.main

import android.app.Application
import asaa.bachelor.bleconnector.logging.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@HiltAndroidApp
class ExampleApplication : Application() {
    private lateinit var fileLoggingTree: FileLoggingTree
    override fun onCreate() {
        super.onCreate()
        val debugTree = Timber.DebugTree()
        Timber.plant(debugTree)

        try {
            fileLoggingTree = FileLoggingTree(debugTree, applicationContext, 200)
        } catch (e: Exception) {
            Timber.e(e)
        }
        Timber.plant(fileLoggingTree)

        Timber.i("Loggers created")
    }

    override fun onTerminate() {
        super.onTerminate()
        fileLoggingTree.writeToFile()
        Timber.uprootAll()
    }

    fun saveLogs() {
        fileLoggingTree.writeToFile()
    }
}