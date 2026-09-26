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
import moe.https.syncthing.core.DevicesController
import moe.https.syncthing.core.NewDeviceConfiguration
import moe.https.syncthing.core.SyncthingPendingDevice
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.model.DevicesUiState
import moe.https.syncthing.ui.model.updateFrom
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

class DevicesViewModel(
    private val controller: DevicesController,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = mutableUiState.asStateFlow()
    private val refreshMutex = Mutex()

    fun refresh() {
        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadDevices())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    fun addDevice(configuration: NewDeviceConfiguration) {
        saveDevice(configuration, false)
    }

    fun updateDevice(configuration: NewDeviceConfiguration) {
        saveDevice(configuration, true)
    }

    fun deleteDevice(deviceId: String) {
        val normalizedDeviceId = deviceId.trim()
        if (normalizedDeviceId.isBlank()) {
            showError(Res.string.device_error_device_id_required)
            return
        }

        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    controller.deleteDevice(normalizedDeviceId)
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadDevices())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    fun pauseDevice(deviceId: String) {
        val normalizedDeviceId = deviceId.trim()
        if (normalizedDeviceId.isBlank()) {
            showError(Res.string.device_error_device_id_required)
            return
        }

        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                val device = mutableUiState.value.devices.firstOrNull {
                    it.id == normalizedDeviceId
                }
                if (device == null) {
                    val errorMessage = getString(Res.string.device_error_device_not_found)
                    mutableUiState.update {
                        it.copy(errorMessage = errorMessage)
                    }
                    return@withLock
                }

                mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    controller.setDevicePaused(normalizedDeviceId, !device.paused)
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadDevices())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    fun dismissPendingDevice(deviceId: String) {
        updatePendingDevice {
            controller.dismissPendingDevice(deviceId)
        }
    }

    fun ignorePendingDevice(device: SyncthingPendingDevice) {
        updatePendingDevice {
            controller.ignorePendingDevice(device)
        }
    }

    private fun updatePendingDevice(
        operation: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isPendingDeviceActionInProgress) return@withLock

                mutableUiState.update {
                    it.copy(isPendingDeviceActionInProgress = true, errorMessage = null)
                }
                try {
                    operation()
                    val pendingDevices = controller.loadPendingDevices()
                    mutableUiState.update {
                        it.copy(
                            pendingDevices = pendingDevices,
                            isPendingDeviceActionInProgress = false,
                        )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isPendingDeviceActionInProgress = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    private fun saveDevice(configuration: NewDeviceConfiguration, updating: Boolean) {
        val normalizedConfiguration = configuration.copy(
            deviceId = configuration.deviceId.trim(),
            name = configuration.name.trim(),
            group = configuration.group.trim(),
            addresses = configuration.addresses
                .map(String::trim)
                .filter(String::isNotBlank),
        )
        if (normalizedConfiguration.deviceId.isBlank()) {
            showError(Res.string.device_error_device_id_required)
            return
        }
        if (
            normalizedConfiguration.numConnections < 0 ||
            normalizedConfiguration.maxSendKiBPerSecond < 0 ||
            normalizedConfiguration.maxReceiveKiBPerSecond < 0
        ) {
            showError(Res.string.device_error_device_limits_nonnegative)
            return
        }

        viewModelScope.launch {
            refreshMutex.withLock {
                if (mutableUiState.value.isLoading) return@withLock

                mutableUiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    if (updating) controller.updateDevice(normalizedConfiguration)
                    else controller.addDevice(normalizedConfiguration)
                    mutableUiState.value = mutableUiState.value.updateFrom(controller.loadDevices())
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val errorMessage = error.userMessage()
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMessage,
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(controller: DevicesController): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DevicesViewModel(controller)
            }
        }
    }

    private fun showError(resource: StringResource) {
        viewModelScope.launch {
            val errorMessage = getString(resource)
            mutableUiState.update { it.copy(errorMessage = errorMessage) }
        }
    }
}

private suspend fun Throwable.userMessage(): String =
    message?.takeIf(String::isNotBlank)
        ?: this::class.simpleName?.takeIf(String::isNotBlank)
        ?: getString(Res.string.common_unknown)
