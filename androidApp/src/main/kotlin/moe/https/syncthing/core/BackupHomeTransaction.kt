package moe.https.syncthing.core

import java.io.File

/** [moveDirectory] 必须使用原子移动，以便仅在成功后记录目录所有权的变化。 */
internal class BackupHomeTransaction(
    private val homeDirectory: File,
    private val stagedHome: File,
    private val previousHome: File,
    private val moveDirectory: (File, File) -> Unit,
    private val deleteDirectory: (File) -> Boolean = { it.deleteRecursively() },
) {
    fun apply(applyChanges: () -> Unit, rollbackChanges: () -> Unit) {
        val hadOriginalHome = homeDirectory.exists()
        var originalMoved = false
        var importedHomeInstalled = false
        var changesStarted = false
        var originalRestored = false
        try {
            if (hadOriginalHome) {
                moveDirectory(homeDirectory, previousHome)
                originalMoved = true
            }
            moveDirectory(stagedHome, homeDirectory)
            importedHomeInstalled = true
            changesStarted = true
            applyChanges()
        } catch (error: Throwable) {
            val rollbackError = runCatching {
                // 只有成功安装的新目录属于本次导入；移动失败时不能清理原目录。
                if (importedHomeInstalled) {
                    if (!deleteDirectory(homeDirectory) || homeDirectory.exists()) {
                        throw LocalizedIOException(CoreMessage(CoreMessageKey.BACKUP_CLEANUP_IMPORTED))
                    }
                }
                if (originalMoved) {
                    if (!previousHome.exists()) throw LocalizedIOException(CoreMessage(CoreMessageKey.BACKUP_ORIGINAL_DIRECTORY_MISSING))
                    moveDirectory(previousHome, homeDirectory)
                    originalRestored = true
                }
                // 目录移动失败时尚未修改设置，也不应通过协调逻辑改写原配置。
                if (changesStarted) rollbackChanges()
            }.exceptionOrNull()
            val message = when {
                originalMoved && !originalRestored ->
                    if (previousHome.exists()) {
                        CoreMessage(CoreMessageKey.BACKUP_RECOVERY_INCOMPLETE_ORIGINAL_RETAINED, previousHome.path)
                    } else {
                        CoreMessage(CoreMessageKey.BACKUP_RECOVERY_INCOMPLETE_ORIGINAL_MISSING, previousHome.path)
                    }
                rollbackError != null && hadOriginalHome ->
                    CoreMessage(CoreMessageKey.BACKUP_RECOVERY_SETTINGS_INCOMPLETE, homeDirectory.path)
                rollbackError != null -> CoreMessage(CoreMessageKey.BACKUP_RECOVERY_NO_ORIGINAL)
                !originalMoved && hadOriginalHome -> CoreMessage(CoreMessageKey.BACKUP_ORIGINAL_UNCHANGED)
                hadOriginalHome -> CoreMessage(CoreMessageKey.BACKUP_ORIGINAL_RESTORED)
                else -> CoreMessage(CoreMessageKey.BACKUP_IMPORT_REVERTED)
            }
            throw LocalizedIOException(
                CoreMessage(CoreMessageKey.BACKUP_OPERATION_DETAIL, message, error),
                error,
            ).also { rollbackError?.let(it::addSuppressed) }
        }
        previousHome.deleteRecursively()
    }
}
