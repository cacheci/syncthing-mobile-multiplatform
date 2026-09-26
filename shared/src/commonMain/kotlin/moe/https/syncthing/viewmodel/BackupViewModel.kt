package moe.https.syncthing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.https.syncthing.core.BackupController
import moe.https.syncthing.core.BackupImportFormat
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.model.BackupUiEffect
import moe.https.syncthing.ui.model.BackupUiState
import moe.https.syncthing.ui.model.PendingBackupImport
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class BackupViewModel(
    private val controller: BackupController,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(BackupUiState())
    val uiState = mutableUiState.asStateFlow()

    private val mutableEffects = MutableSharedFlow<BackupUiEffect>(extraBufferCapacity = 1)
    val effects = mutableEffects.asSharedFlow()

    private var pendingExportPassword: String? = null

    fun requestExport(password: String?) {
        if (mutableUiState.value.isWorking) return
        pendingExportPassword = password?.takeIf(String::isNotEmpty)
        mutableEffects.tryEmit(
            BackupUiEffect.CreateDocument(
                suggestedFileName = if (pendingExportPassword == null) {
                    "syncthing-backup.zip"
                } else {
                    "syncthing-backup-encrypted.zip"
                },
            ),
        )
    }

    fun onExportDestinationSelected(destinationUri: String?) {
        val password = pendingExportPassword
        pendingExportPassword = null
        if (destinationUri == null) return
        runOperation(successMessage = Res.string.setting_backup_export_succeeded) {
            controller.exportBackup(destinationUri, password)
        }
    }

    fun requestImport(format: BackupImportFormat) {
        if (mutableUiState.value.isWorking) return
        mutableEffects.tryEmit(BackupUiEffect.OpenDocument(format))
    }

    fun onImportSourceSelected(sourceUri: String?, format: BackupImportFormat) {
        if (sourceUri == null) return
        mutableUiState.update {
            it.copy(
                pendingImport = PendingBackupImport(sourceUri, format),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun cancelImport() {
        if (mutableUiState.value.isWorking) return
        mutableUiState.update { it.copy(pendingImport = null) }
    }

    fun confirmImport(password: String?) {
        if (mutableUiState.value.isWorking) return
        val pendingImport = mutableUiState.value.pendingImport ?: return
        mutableUiState.update { it.copy(pendingImport = null) }
        runOperation(
            successMessage = Res.string.setting_backup_import_succeeded,
        ) {
            controller.importBackup(
                sourceUri = pendingImport.sourceUri,
                password = password?.takeIf(String::isNotEmpty),
                format = pendingImport.format,
            )
        }
    }

    fun clearMessage() {
        mutableUiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    private fun runOperation(
        successMessage: StringResource,
        onSuccess: (BackupUiState) -> BackupUiState = { it },
        operation: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            mutableUiState.update {
                it.copy(isWorking = true, successMessage = null, errorMessage = null)
            }
            try {
                operation()
                val localizedSuccessMessage = getString(successMessage)
                mutableUiState.update {
                    onSuccess(it.copy(isWorking = false, successMessage = localizedSuccessMessage))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val errorMessage = error.message
                    ?.takeIf(String::isNotBlank)
                    ?: error::class.simpleName
                    ?: getString(Res.string.setting_backup_operation_failed)
                mutableUiState.update {
                    it.copy(
                        isWorking = false,
                        errorMessage = errorMessage,
                    )
                }
            }
        }
    }

    companion object {
        fun factory(controller: BackupController): ViewModelProvider.Factory = viewModelFactory {
            initializer { BackupViewModel(controller) }
        }
    }
}
