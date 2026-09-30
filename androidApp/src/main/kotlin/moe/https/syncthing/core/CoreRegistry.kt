package moe.https.syncthing.core

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import java.io.IOException

class CoreRegistry internal constructor(
    context: Context,
    private val builtInProvider: BuiltInCoreProvider,
    private val externalInstaller: ExternalCoreInstaller,
) {
    private val preferences = context.applicationContext.getSharedPreferences(
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
                        "当前系统不允许执行此外置核心"
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
            throw IOException("内置核心不能删除")
        }
        if (id == selectedId) throw IOException("不能删除当前选中的核心，请先选择其他核心")
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
                    throw IOException("当前系统不允许执行所选外置核心，请使用内置核心")
                }
            }
            ?: throw IOException("所选外置核心不存在")
    }

    companion object {
        const val BUILT_IN_ID = "builtin"
        const val BUILT_IN_PREVIOUS_STABLE_ID = "builtin-previous-stable"
        const val BUILT_IN_RC_ID = "builtin-rc"
        private const val PREFERENCES = "core_registry"
        private const val KEY_SELECTED_ID = "selected_id"
    }
}
