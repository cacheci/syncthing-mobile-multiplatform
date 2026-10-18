package moe.https.syncthing.ui.model

import moe.https.syncthing.core.FoldersSnapshot
import moe.https.syncthing.core.SyncthingFolder
import moe.https.syncthing.core.SyncthingPendingFolder

data class FoldersUiState(
    val folders: List<SyncthingFolder> = emptyList(),
    val pendingFolders: List<SyncthingPendingFolder> = emptyList(),
    val isLoading: Boolean = false,
    val isPendingFolderActionInProgress: Boolean = false,
    val hasLoaded: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null,
    val rescanningFolderIds: Set<String> = emptySet(),
    val isPausingAllFolders: Boolean = false,
    val isResumingAllFolders: Boolean = false,
    val isRescanningAllFolders: Boolean = false,
) {
    val showPauseAllFolders: Boolean
        get() = folders.any { !it.paused }

    val showResumeAllFolders: Boolean
        get() = folders.any { it.paused }

    val canPauseAllFolders: Boolean
        get() = !isLoading && !isPausingAllFolders && !isResumingAllFolders &&
            !isRescanningAllFolders && showPauseAllFolders

    val canResumeAllFolders: Boolean
        get() = !isLoading && !isPausingAllFolders && !isResumingAllFolders &&
            !isRescanningAllFolders && showResumeAllFolders

    val canRescanAllFolders: Boolean
        get() = !isLoading && !isPausingAllFolders && !isResumingAllFolders && !isRescanningAllFolders &&
            rescanningFolderIds.isEmpty() && folders.any {
                !it.paused && it.state != "scanning" && it.state != "scan-wait"
            }
}

fun FoldersUiState.updateFrom(snapshot: FoldersSnapshot): FoldersUiState = copy(
    folders = snapshot.folders,
    pendingFolders = snapshot.pendingFolders,
    isLoading = false,
    isPendingFolderActionInProgress = false,
    hasLoaded = true,
    loadError = null,
)
