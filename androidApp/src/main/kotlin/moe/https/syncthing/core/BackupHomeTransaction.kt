package moe.https.syncthing.core

import java.io.File
import java.io.IOException

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
                        throw IOException("无法清理本次导入的配置目录")
                    }
                }
                if (originalMoved) {
                    if (!previousHome.exists()) throw IOException("原配置备份目录不存在")
                    moveDirectory(previousHome, homeDirectory)
                    originalRestored = true
                }
                // 目录移动失败时尚未修改设置，也不应通过协调逻辑改写原配置。
                if (changesStarted) rollbackChanges()
            }.exceptionOrNull()
            val message = when {
                originalMoved && !originalRestored ->
                    if (previousHome.exists()) {
                        "导入失败且自动恢复未完成，原配置保留在 ${previousHome.path}"
                    } else {
                        "导入失败且自动恢复未完成，无法找到原配置备份目录 ${previousHome.path}"
                    }
                rollbackError != null && hadOriginalHome ->
                    "导入失败，原配置目录已恢复到 ${homeDirectory.path}，但设置或运行时恢复未完成"
                rollbackError != null -> "导入失败且自动恢复未完成，导入前没有原配置目录"
                !originalMoved && hadOriginalHome -> "导入失败，原配置目录未被替换"
                hadOriginalHome -> "导入失败，已恢复原配置"
                else -> "导入失败，已撤销导入，导入前没有原配置目录"
            }
            throw IOException(
                "$message：${error.message ?: error.javaClass.simpleName}",
                error,
            ).also { rollbackError?.let(it::addSuppressed) }
        }
        previousHome.deleteRecursively()
    }
}
