package asaa.bachelor.bleconnector.logging

import android.content.Context
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


class FileLoggingTree(val loggingTree: Timber.DebugTree, private val context: Context, val logCacheSize: Int = 400) : Timber.DebugTree() {
    private val logsCache = mutableListOf<String>()
    private val path = "Log"
    private val delimiter = "$$$"
    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(Date())
    var file: File? = generateFile(path, "$date.log")

    @Synchronized
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val currTimeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.GERMANY).format(Date())
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

    @Synchronized
    fun writeToFile() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                runBlocking {
                    loggingTree.i("write To file: ${file?.absolutePath}")
                    if (file == null) return@runBlocking
                    try {
                        FileWriter(file, true).apply {
                            append(logsCache.joinToString("\n", prefix = "\n"))
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