package moe.https.syncthing.core

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import moe.https.syncthing.R

class CoreRegistry internal constructor(
    context: Context,
    private val builtInProvider: BuiltInCoreProvider,
    private val externalInstaller: ExternalCoreInstaller,
) {
    private val applicationContext = context.applicationContext
    private val preferences = applicationContext.getSharedPreferences(
        PREFERENCES,
        Context.MODE_PRIVATE,
    )

    internal val selectedId: String
        get() = preferences.getString(KEY_SELECTED_ID, BUILT_IN_ID) ?: BUILT_IN_ID

    internal fun availableOptions(): List<CoreOption> = buildList {
        addAll(builtInProvider.options())
        externalInstaller.installedCores().forEach { core ->
            val availability = if (core.file.canExecute()) {
                CoreAvailability.AVAILABLE
            } else {
                CoreAvailability.EXECUTION_UNSUPPORTED
            }
            add(
                CoreOption(
                    id = core.id,
                    internal = false,
                    version = core.version,
                    source = CoreSource.EXTERNAL,
                    availability = availability,
                    unavailableReason = if (availability == CoreAvailability.AVAILABLE) {
                        null
                    } else {
                        applicationContext.getString(R.string.core_external_unexecutable_reason)
                    },
                ),
            )
        }
    }

    internal fun selectedOption(): CoreOption {
        val options = availableOptions()
        return options.firstOrNull { it.id == selectedId }
            ?: options.first { it.id == BUILT_IN_ID }.also {
                preferences.edit { putString(KEY_SELECTED_ID, BUILT_IN_ID) }
            }
    }

    internal fun resolveSelected(): CoreExecutable = resolve(selectedOption().id)

    internal fun select(id: String): CoreExecutable {
        val executable = resolve(id)
        preferences.edit { putString(KEY_SELECTED_ID, executable.id) }
        return executable
    }

    internal fun importAndSelect(uri: Uri): ExternalCoreExecutable {
        val executable = externalInstaller.install(uri)
        preferences.edit { putString(KEY_SELECTED_ID, executable.id) }
        return executable
    }

    internal fun deleteExternal(id: String): ExternalCoreExecutable {
        if (id == BUILT_IN_ID || id == BUILT_IN_PREVIOUS_STABLE_ID || id == BUILT_IN_RC_ID) {
            throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_BUILT_IN_DELETE))
        }
        if (id == selectedId) throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_SELECTED_DELETE))
        return externalInstaller.delete(id)
    }

    internal fun knownExecutablePaths(): Set<String> = buildSet {
        builtInProvider.options().forEach { option ->
            runCatching { builtInProvider.resolve(option.id).file.absolutePath }
                .getOrNull()
                ?.let(::add)
        }
        externalInstaller.installedCores().forEach { add(it.file.absolutePath) }
    }

    private fun resolve(id: String): CoreExecutable = when (id) {
        BUILT_IN_ID, BUILT_IN_PREVIOUS_STABLE_ID, BUILT_IN_RC_ID -> builtInProvider.resolve(id)
        else -> externalInstaller.installedCores().firstOrNull { it.id == id }
            ?.also {
                if (!it.file.canExecute()) {
                    throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_SELECTED_UNSUPPORTED))
                }
            }
            ?: throw LocalizedIOException(CoreMessage(CoreMessageKey.CORE_EXTERNAL_MISSING))
    }

    companion object {
        const val BUILT_IN_ID = "builtin"
        const val BUILT_IN_PREVIOUS_STABLE_ID = "builtin-previous-stable"
        const val BUILT_IN_RC_ID = "builtin-rc"
        private const val PREFERENCES = "core_registry"
        private const val KEY_SELECTED_ID = "selected_id"
    }
}
