package moe.https.syncthing.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.https.syncthing.R
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidCoreLogReader(context: Context) : CoreLogReader {
    private val applicationContext = context.applicationContext
    private val logDirectory = File(applicationContext.filesDir, "logs")

    override suspend fun read(source: CoreLogSource): CoreLogContent = withContext(Dispatchers.IO) {
        val file = when (source) {
            CoreLogSource.SYNCTHING -> File(logDirectory, "syncthing.log")
            CoreLogSource.LAUNCHER -> File(logDirectory, "launcher.log")
            CoreLogSource.CONTROLLER -> File(logDirectory, "controller.log")
        }
        CoreLogContent(
            text = readTail(file),
            refreshedAt = SimpleDateFormat(
                applicationContext.getString(R.string.log_refresh_time_format),
                Locale.getDefault(),
            ).format(Date()),
        )
    }

    private fun readTail(file: File): String {
        if (!file.isFile || file.length() == 0L) return ""
        RandomAccessFile(file, "r").use { input ->
            val start = (input.length() - MAX_LOG_BYTES).coerceAtLeast(0)
            input.seek(start)
            val bytes = ByteArray((input.length() - start).toInt())
            input.readFully(bytes)
            val decoded = bytes.toString(Charsets.UTF_8)
            val completeText = if (start > 0) decoded.substringAfter('\n', "") else decoded
            return completeText.lineSequence()
                .toList()
                .takeLast(MAX_LOG_LINES)
                .joinToString("\n")
        }
    }

    companion object {
        private const val MAX_LOG_BYTES = 256L * 1024L
        private const val MAX_LOG_LINES = 500
    }
}
