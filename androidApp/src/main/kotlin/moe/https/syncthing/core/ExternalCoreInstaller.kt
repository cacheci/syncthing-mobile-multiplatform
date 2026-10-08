package moe.https.syncthing.core

import android.content.Context
import android.net.Uri
import android.os.Build
import android.system.ErrnoException
import android.system.Os
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class ExternalCoreInstaller(context: Context) {
    private val applicationContext = context.applicationContext
    private val externalDirectory: File
        get() = File(applicationContext.filesDir, "core/external")
    private val importDirectory: File
        get() = File(applicationContext.cacheDir, "core-import")

    fun installedCores(): List<ExternalCoreExecutable> = externalDirectory
        .listFiles { file -> file.isDirectory }
        .orEmpty()
        .mapNotNull(::readInstalledCore)
        .sortedWith(
            compareByDescending<ExternalCoreExecutable> { it.importedAtMillis }
                .thenBy { it.id },
        )

    fun install(uri: Uri): ExternalCoreExecutable {
        val temporaryDirectory = importDirectory.apply {
            if (!exists() && !mkdirs()) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_IMPORT_DIRECTORY))
        }
        val candidate = File(temporaryDirectory, "candidate-${UUID.randomUUID()}")
        try {
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                candidate.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            } ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_READ_SELECTED))

            validateElf(candidate)
            setExecutable(candidate)
            val version = readVersion(candidate)
            val id = UUID.randomUUID().toString()
            val importedAt = System.currentTimeMillis()
            val sha256 = sha256(candidate)
            val targetDirectory = File(externalDirectory, id)
            if (!targetDirectory.mkdirs()) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_CREATE_EXTERNAL_DIRECTORY))
            val target = File(targetDirectory, BINARY_FILE_NAME)
            try {
                move(candidate, target)
                setExecutable(target)
                File(targetDirectory, METADATA_FILE_NAME).writeText(
                    JSONObject()
                        .put("id", id)
                        .put("version", version)
                        .put("importedAtMillis", importedAt)
                        .put("sha256", sha256)
                        .toString(),
                )
            } catch (error: Exception) {
                target.delete()
                File(targetDirectory, METADATA_FILE_NAME).delete()
                targetDirectory.delete()
                throw error
            }
            return ExternalCoreExecutable(id, version, target, importedAt, sha256)
        } finally {
            candidate.delete()
        }
    }

    fun delete(id: String): ExternalCoreExecutable {
        val core = installedCores().firstOrNull { installed -> installed.id == id }
            ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_MISSING))
        val root = externalDirectory.canonicalFile
        val directory = core.file.parentFile?.canonicalFile
            ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_DIRECTORY_INVALID))
        if (directory.parentFile != root || directory.name != core.id) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_DELETE_OUTSIDE))
        }

        val entries = directory.listFiles()
            ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_READ_DIRECTORY))
        val allowedNames = setOf(BINARY_FILE_NAME, METADATA_FILE_NAME)
        if (entries.any { entry -> !entry.isFile || entry.name !in allowedNames }) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_UNKNOWN_FILES))
        }
        entries.forEach { entry ->
            if (!entry.delete()) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_DELETE_FILE, entry.name))
        }
        if (!directory.delete()) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_DELETE_DIRECTORY))
        return core
    }

    private fun readInstalledCore(directory: File): ExternalCoreExecutable? = runCatching {
        val metadata = JSONObject(File(directory, METADATA_FILE_NAME).readText())
        val id = metadata.getString("id")
        if (directory.name != id) return@runCatching null
        val file = File(directory, BINARY_FILE_NAME)
        if (!file.isFile) return@runCatching null
        ExternalCoreExecutable(
            id = id,
            version = metadata.getString("version"),
            file = file,
            importedAtMillis = metadata.getLong("importedAtMillis"),
            sha256 = metadata.getString("sha256"),
        )
    }.getOrNull()

    private fun validateElf(file: File) {
        val header = ByteArray(20)
        file.inputStream().use { input ->
            var offset = 0
            while (offset < header.size) {
                val count = input.read(header, offset, header.size - offset)
                if (count < 0) break
                offset += count
            }
            if (offset < header.size) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_ELF_TOO_SHORT))
        }
        val hasElfMagic =
            header[0] == 0x7F.toByte() &&
                header[1] == 'E'.code.toByte() &&
                header[2] == 'L'.code.toByte() &&
                header[3] == 'F'.code.toByte()
        if (!hasElfMagic) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_ELF_INVALID))
        val elfClass = header[4].toInt() and 0xFF
        if (header[5].toInt() != ELF_LITTLE_ENDIAN) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_ELF_ENDIAN))
        val machine =
            (header[18].toInt() and 0xFF) or
                ((header[19].toInt() and 0xFF) shl 8)
        val abi = ELF_ABIS[elfClass to machine]
            ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_ELF_ARCHITECTURE))
        if (abi !in Build.SUPPORTED_ABIS) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_ELF_INCOMPATIBLE, abi))
        }
    }

    private fun setExecutable(file: File) {
        try {
            Os.chmod(file.absolutePath, EXECUTABLE_MODE)
        } catch (error: ErrnoException) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_EXECUTABLE_PERMISSION), error)
        }
    }

    private fun readVersion(file: File): String {
        val outputFile = File(importDirectory, "version-${UUID.randomUUID()}.txt")
        val redirectedProcess = try {
            ProcessBuilder(file.absolutePath, "--version")
                .apply {
                    environment()["HOME"] = applicationContext.filesDir.absolutePath
                    environment()["STNOUPGRADE"] = "1"
                }
                .startRedirectingOutputCompat(outputFile)
        } catch (error: IOException) {
            if (error.message.orEmpty().contains("Permission denied", ignoreCase = true)) {
                throw LocalizedIOException(
                    CoreMessage(CoreMessageKey.CORE_EXTERNAL_EXECUTION_UNSUPPORTED),
                    error,
                )
            }
            throw error
        }
        val process = redirectedProcess.process
        try {
            if (!process.waitForCompat(VERSION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForciblyCompat()
                throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_VERSION_TIMEOUT))
            }
            redirectedProcess.awaitOutput()
            val buffer = ByteArray(MAX_VERSION_LENGTH + 1)
            val count = outputFile.inputStream().use { it.read(buffer) }.coerceAtLeast(0)
            val output = buffer.decodeToString(0, count)
                .lineSequence()
                .firstOrNull()
                .orEmpty()
                .trim()
                .take(MAX_VERSION_LENGTH)
            if (process.exitValue() != 0 || output.isBlank()) {
                throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_RUN_FAILED, output.takeIf(String::isNotBlank) ?: CoreMessage(CoreMessageKey.UNKNOWN_ERROR)))
            }
            if (!output.contains("syncthing", ignoreCase = true)) {
                throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_VERSION_INVALID))
            }
            return output
        } finally {
            outputFile.delete()
        }
    }

    private fun move(source: File, target: File) {
        try {
            Files.move(
                source.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath())
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    companion object {
        private const val BINARY_FILE_NAME = "syncthing"
        private const val METADATA_FILE_NAME = "metadata.json"
        private const val EXECUTABLE_MODE = 448 // 0700
        private const val ELF_CLASS_32 = 1
        private const val ELF_CLASS_64 = 2
        private const val ELF_LITTLE_ENDIAN = 1
        private const val ELF_MACHINE_X86 = 3
        private const val ELF_MACHINE_ARM = 40
        private const val ELF_MACHINE_X86_64 = 62
        private const val ELF_MACHINE_AARCH64 = 183
        private const val VERSION_TIMEOUT_SECONDS = 5L
        private const val MAX_VERSION_LENGTH = 160
        private val ELF_ABIS = mapOf(
            (ELF_CLASS_64 to ELF_MACHINE_AARCH64) to "arm64-v8a",
            (ELF_CLASS_32 to ELF_MACHINE_ARM) to "armeabi-v7a",
            (ELF_CLASS_32 to ELF_MACHINE_X86) to "x86",
            (ELF_CLASS_64 to ELF_MACHINE_X86_64) to "x86_64",
        )
    }
}
