package moe.https.syncthing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.https.syncthing.core.FoldersController
import moe.https.syncthing.core.NewFolderConfiguration
import moe.https.syncthing.core.SyncthingPendingFolder
import moe.https.syncthing.core.localizedUserMessageOrNull
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.model.FoldersUiState
import moe.https.syncthing.ui.model.updateFrom
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class FoldersViewModel(
    private val controller: FoldersController,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(FoldersUiState())
    val uiState: StateFlow<FoldersUiState> = mutableUiState.asStateFlow()
    private val refreshMutex = Mutex()

    fun refresh() = refresh(showLoading = true)

    fun refreshInBackground() = refresh(showLoading = false)

    private fun refresh(showLoading: Boolean) {
        viewModelScope.launch {
            // Repeated refreshes must not queue behind a slow request.
            if (!refreshMutex.tryLock()) return@launch
            try {
                refreshSnapshot(showLoading)
            } finally {
                refreshMutex.unlock()
            }
        }
    }

    private suspend fun refreshSnapshot(showLoading: Boolean) {
        mutableUiState.update {
            it.copy(isLoading = showLoading || !it.hasLoaded, loadError = null)
        }
        try {
            val snapshot = controller.loadFolders()
            mutableUiState.update { it.updateFrom(snapshot) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            val errorMessage = error.userMessage()
            mutableUiState.update {
                it.copy(hasLoaded = true, loadError = errorMessage)
            }
        } finally {
            mutableUiState.update { it.copy(isLoading = false) }
        }
    }

    fun rescanFolder(folderId: String) {
        val normalizedFolderId = folderId.trim()
        if (normalizedFolderId.isBlank()) {
            showError(Res.string.folder_error_folder_id_required)
            return
        }
        val state = mutableUiState.value
        val folder = state.folders.firstOrNull { it.id == normalizedFolderId } ?: return
        if (state.isLoading || folder.paused ||
            folder.state == "scanning" || folder.state == "scan-wait" ||
            normalizedFolderId in state.rescanningFolderIds
        ) return

        mutableUiState.update {
            it.copy(
                rescanningFolderIds = it.rescanningFolderIds + normalizedFolderId,
                actionError = null,
            )
        }
        viewModelScope.launch {
            try {
                // Scans can take minutes; do not hold the snapshot refresh mutex.
                controller.rescanFolder(normalizedFolderId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val errorMessage = error.userMessage()
                mutableUiState.update { it.copy(actionError = errorMessage) }
            } finally {
                mutableUiState.update {
                    it.copy(rescanningFolderIds = it.rescanningFolderIds - normalizedFolderId)
                }
            }
            // A refresh started before scan completion may contain old statistics.
            // Always fetch once more after it finishes, rather than dropping this refresh.
            refreshMutex.withLock { refreshSnapshot(showLoading = false) }
        }
    }

    fun addFolder(configuration: NewFolderConfiguration) {
        saveFolder(configuration, updating = false)
    }

    fun updateFolder(configuration: NewFolderConfiguration) {
        saveFolder(configuration, updating = true)
    }

    fun deleteFolder(
        folderId: String,
        deleteLocalFiles: Boolean,
        onSuccess: () -> Unit = {},
    ) {
        updateConfiguredFolder(folderId, onSuccess) { normalizedFolderId ->
            controller.deleteFolder(normalizedFolderId, deleteLocalFiles)
        }
    }

    fun setFolderPaused(folderId: String, paused: Boolean) {
        updateConfiguredFolder(folderId) { normalizedFolderId ->
            controller.setFolderPaused(normalizedFolderId, paused)
        }
    }

    fun dismissPendingFolder(folder: SyncthingPendingFolder) {
        updatePendingFolder {
            controller.dismissPendingFolder(folder)
        }
    }

    fun ignorePendingFolder(folder: SyncthingPendingFolder) {
        updatePendingFolder {
            controller.ignorePendingFolder(folder)
        }
    }

    private fun updatePendingFolder(
        operation: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isPendingFolderActionInProgress) return@withLock

                mutableUiState.update {
                    it.copy(isPendingFolderActionInProgress = true, actionError = null)
                }
                try {
                    operation()
                    val pendingFolders = controller.loadPendingFolders()
                    mutableUiState.update {
                        it.copy(
                            pendingFolders = pendingFolders,
                            isPendingFolderActionInProgress = false,
                        )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isPendingFolderActionInProgress = false,
                            actionError = errorMessage,
                        )
                    }
                }
            }
        }
    }

    private fun updateConfiguredFolder(
        folderId: String,
        onSuccess: () -> Unit = {},
        operation: suspend (String) -> Unit,
    ) {
        val normalizedFolderId = folderId.trim()
        if (normalizedFolderId.isBlank()) {
            showError(Res.string.folder_error_folder_id_required)
            return
        }

        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, actionError = null) }
                try {
                    operation(normalizedFolderId)
                    val snapshot = controller.loadFolders()
                    mutableUiState.update { it.updateFrom(snapshot) }
                    onSuccess()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            actionError = errorMessage,
                        )
                    }
                }
            }
        }
    }

    private fun saveFolder(configuration: NewFolderConfiguration, updating: Boolean) {
        val normalizedConfiguration = configuration.copy(
            folderId = configuration.folderId.trim(),
            label = configuration.label.trim().ifBlank {
                configuration.folderId.trim()
            },
            group = configuration.group.trim(),
            path = configuration.path.trim(),
            devices = configuration.devices.map { device ->
                device.copy(deviceId = device.deviceId.trim())
            },
            availableDeviceIds = configuration.availableDeviceIds.map(String::trim).toSet(),
        )
        val validationMessage = when {
            normalizedConfiguration.folderId.isBlank() -> Res.string.folder_error_folder_id_required
            normalizedConfiguration.path.isBlank() -> Res.string.folder_error_folder_path_required
            normalizedConfiguration.versioningCleanoutDays < 0 ||
                normalizedConfiguration.versioningKeep < 0 ||
                normalizedConfiguration.versioningCleanupIntervalSeconds < 0 ||
                normalizedConfiguration.rescanIntervalSeconds < 0 -> Res.string.folder_error_time_count_nonnegative
            normalizedConfiguration.versioningCleanupIntervalSeconds > 31_536_000 ->
                Res.string.folder_error_cleanup_interval_year
            normalizedConfiguration.versioning == NewFolderConfiguration.Versioning.EXTERNAL &&
                normalizedConfiguration.versioningExternalCommand.isBlank() -> Res.string.folder_error_external_command_required
            else -> null
        }
        if (validationMessage != null) {
            showError(validationMessage)
            return
        }

        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, actionError = null) }
                try {
                    if (updating) controller.updateFolder(normalizedConfiguration)
                    else controller.addFolder(normalizedConfiguration)
                    val snapshot = controller.loadFolders()
                    mutableUiState.update { it.updateFrom(snapshot) }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            actionError = errorMessage,
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(controller: FoldersController): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FoldersViewModel(controller)
            }
        }
    }

    private fun showError(resource: StringResource) {
        viewModelScope.launch {
            val errorMessage = getString(resource)
            mutableUiState.update { it.copy(actionError = errorMessage) }
        }
    }
}

private suspend fun Throwable.userMessage(): String =
    localizedUserMessageOrNull()
        ?: this::class.simpleName?.takeIf(String::isNotBlank)
        ?: getString(Res.string.common_unknown)
