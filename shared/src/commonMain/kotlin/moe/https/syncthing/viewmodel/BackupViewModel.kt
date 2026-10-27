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
import moe.https.syncthing.core.PendingMessage
import moe.https.syncthing.core.PendingMessages
import moe.https.syncthing.core.localizedUserMessageOrNull
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

    private val pendingMessages = PendingMessages<String>()
    val messages = pendingMessages.messages

    fun consumeMessages(message: PendingMessage<String>): Boolean =
        pendingMessages.consume(message)

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

    private fun runOperation(
        successMessage: StringResource,
        onSuccess: (BackupUiState) -> BackupUiState = { it },
        operation: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isWorking = true) }
            val workingMessage = pendingMessages.add(getString(Res.string.setting_backup_working))
            try {
                operation()
                pendingMessages.add(getString(successMessage))
                mutableUiState.update {
                    onSuccess(it.copy(isWorking = false))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val errorMessage = error.localizedUserMessageOrNull()
                    ?: error::class.simpleName
                    ?: getString(Res.string.setting_backup_operation_failed)
                pendingMessages.add(errorMessage)
                mutableUiState.update {
                    it.copy(isWorking = false)
                }
            } finally {
                // A completed operation must not deliver a stale progress notice on return.
                pendingMessages.consume(workingMessage)
            }
        }
    }

    companion object {
        fun factory(controller: BackupController): ViewModelProvider.Factory = viewModelFactory {
            initializer { BackupViewModel(controller) }
        }
    }
}
