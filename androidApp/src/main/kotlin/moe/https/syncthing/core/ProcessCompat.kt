package moe.https.syncthing.core

import android.os.Build
import android.os.SystemClock
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class RedirectedProcess internal constructor(
    val process: Process,
    private val outputPump: Thread?,
    private val outputFailure: AtomicReference<IOException?>?,
    private val outputFile: File,
) {
    fun awaitOutput() {
        outputPump?.join()
        outputFailure?.get()?.let { error ->
            throw IOException("无法写入进程输出：${outputFile.path}", error)
        }
    }
}

internal fun ProcessBuilder.startRedirectingOutputCompat(outputFile: File): RedirectedProcess {
    redirectErrorStream(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ProcessApi26.redirectOutput(this, outputFile)
        return RedirectedProcess(start(), null, null, outputFile)
    }

    val output = outputFile.outputStream().buffered()
    val process = try {
        start()
    } catch (error: Throwable) {
        runCatching { output.close() }
        throw error
    }
    val outputFailure = AtomicReference<IOException?>()
    val outputPump = Thread(
        {
            process.inputStream.use { input ->
                try {
                    output.use { target -> input.copyTo(target) }
                } catch (error: IOException) {
                    outputFailure.compareAndSet(null, error)
                    runCatching { input.copyTo(DISCARD_OUTPUT_STREAM) }
                }
            }
        },
        "process-output-${outputFile.name}",
    ).apply {
        isDaemon = true
        start()
    }
    return RedirectedProcess(process, outputPump, outputFailure, outputFile)
}

internal fun Process.isAliveCompat(): Boolean = try {
    exitValue()
    false
} catch (_: IllegalThreadStateException) {
    true
}

internal fun Process.waitForCompat(timeout: Long, unit: TimeUnit): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return ProcessApi26.waitFor(this, timeout, unit)
    }

    val timeoutMillis = unit.toMillis(timeout)
    val deadline = SystemClock.elapsedRealtime() + timeoutMillis
    while (isAliveCompat()) {
        val remainingMillis = deadline - SystemClock.elapsedRealtime()
        if (remainingMillis <= 0L) return false
        Thread.sleep(minOf(remainingMillis, PROCESS_WAIT_POLL_MILLIS))
    }
    return true
}

internal fun Process.destroyForciblyCompat() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ProcessApi26.destroyForcibly(this)
    } else {
        destroy()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private object ProcessApi26 {
    @DoNotInline
    fun redirectOutput(builder: ProcessBuilder, outputFile: File) {
        builder.redirectOutput(outputFile)
    }

    @DoNotInline
    fun waitFor(process: Process, timeout: Long, unit: TimeUnit): Boolean =
        process.waitFor(timeout, unit)

    @DoNotInline
    fun destroyForcibly(process: Process) {
        process.destroyForcibly()
    }
}

private val DISCARD_OUTPUT_STREAM = object : OutputStream() {
    override fun write(value: Int) = Unit

    override fun write(buffer: ByteArray, offset: Int, length: Int) = Unit
}

private const val PROCESS_WAIT_POLL_MILLIS = 50L
