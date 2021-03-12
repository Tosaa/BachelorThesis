package asaa.bachelor.bleconnector.logging

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


class FileLoggingTree(val loggingTree: Timber.DebugTree, private val context: Context, val logCacheSize: Int = 400) : Timber.DebugTree() {
    private val logsCache = mutableListOf<String>()
    private val path = "Log"
    private val delimiter = "$$$"
    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN).format(Date())
    var file: File? = generateFile(path, "$date.log")

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val currTimeStamp = SimpleDateFormat("hh:mm:ss:SSS", Locale.GERMAN).format(Date())
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                listOf<String>(currTimeStamp, priority.toString(), tag ?: "NO_TAG", message, t.toString() ?: "").joinToString(separator = delimiter.toString()).let {
                    logsCache.add(it)
                }
                if (logsCache.size >= logCacheSize) {
                    writeToFile()
                }
            }
        }
    }

    fun writeToFile() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                loggingTree.i("write To file: ${file?.absolutePath}")
                if (file == null) return@withContext
                try {
                    FileWriter(file, true).apply {
                        append(logsCache.joinToString("\n"))
                        flush()
                        close()
                    }
                    logsCache.clear()
                    loggingTree.i("saved logs")
                } catch (e: Exception) {
                    loggingTree.e(e.toString())
                }
            }
        }
    }


    private fun generateFile(path: String, fileName: String): File? {
        val filePath = File(context.filesDir.absolutePath + File.separator + path)
        if (!filePath.exists()) {
            loggingTree.i("create filepath: $filePath")
            filePath.mkdirs()
        }
        return File(filePath, fileName)
    }

}