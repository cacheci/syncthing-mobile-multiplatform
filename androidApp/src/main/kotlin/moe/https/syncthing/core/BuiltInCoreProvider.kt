package moe.https.syncthing.core

import android.content.Context
import moe.https.syncthing.BuildConfig
import moe.https.syncthing.R
import java.io.File

internal class BuiltInCoreProvider(context: Context) {
    private val applicationContext = context.applicationContext

    private fun binaryFile(definition: Definition): File = File(
        applicationContext.applicationInfo.nativeLibraryDir,
        definition.binaryFileName,
    )

    fun resolve(id: String = CoreRegistry.BUILT_IN_ID): BuiltInCoreExecutable {
        val definition = definition(id) ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_BUILT_IN_MISSING))
        val file = binaryFile(definition)
        if (!file.isFile) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_BUILT_IN_BINARY_MISSING, file.absolutePath))
        }
        if (!file.canExecute()) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_BUILT_IN_NOT_EXECUTABLE))
        }
        return BuiltInCoreExecutable(
            version = definition.version,
            file = file,
            id = definition.id,
        )
    }

    fun options(): List<CoreOption> = definitions().map(::option)

    private fun option(definition: Definition): CoreOption {
        val file = binaryFile(definition)
        val availability = when {
            !file.isFile -> CoreAvailability.MISSING
            !file.canExecute() -> CoreAvailability.EXECUTION_UNSUPPORTED
            else -> CoreAvailability.AVAILABLE
        }
        return CoreOption(
            id = definition.id,
            internal = true,
            version = definition.version,
            source = CoreSource.BUILT_IN,
            availability = availability,
            unavailableReason = when (availability) {
                CoreAvailability.AVAILABLE -> null
                CoreAvailability.MISSING -> applicationContext.getString(R.string.core_builtin_missing_reason)
                CoreAvailability.EXECUTION_UNSUPPORTED -> applicationContext.getString(R.string.core_builtin_unexecutable_reason)
            },
        )
    }

    internal data class Definition(
        val id: String,
        val version: String,
        val commit: String,
        val binaryFileName: String,
    )

    companion object {
        fun definitions(): List<Definition> = buildList {
            add(
                Definition(
                    id = CoreRegistry.BUILT_IN_ID,
                    version = BuildConfig.SYNCTHING_VERSION,
                    commit = BuildConfig.SYNCTHING_COMMIT,
                    binaryFileName = "libsyncthingnative.so",
                ),
            )
            if (BuildConfig.HAS_SYNCTHING_PREVIOUS_STABLE) {
                add(
                    Definition(
                        id = CoreRegistry.BUILT_IN_PREVIOUS_STABLE_ID,
                        version = BuildConfig.SYNCTHING_PREVIOUS_VERSION,
                        commit = BuildConfig.SYNCTHING_PREVIOUS_COMMIT,
                        binaryFileName = "libsyncthingpreviousnative.so",
                    ),
                )
            }
            if (BuildConfig.HAS_SYNCTHING_RC) {
                add(
                    Definition(
                        id = CoreRegistry.BUILT_IN_RC_ID,
                        version = BuildConfig.SYNCTHING_RC_VERSION,
                        commit = BuildConfig.SYNCTHING_RC_COMMIT,
                        binaryFileName = "libsyncthingrcnative.so",
                    ),
                )
            }
        }

        fun definition(id: String): Definition? = definitions().firstOrNull { it.id == id }
    }
}
