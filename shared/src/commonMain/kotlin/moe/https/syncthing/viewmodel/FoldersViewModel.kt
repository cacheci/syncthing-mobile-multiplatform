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

    fun refresh() {
        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, loadError = null) }
                try {
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadFolders())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            loadError = errorMessage,
                        )
                    }
                }
            }
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
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadFolders())
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
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadFolders())
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
    message?.takeIf(String::isNotBlank)
        ?: this::class.simpleName?.takeIf(String::isNotBlank)
        ?: getString(Res.string.common_unknown)
