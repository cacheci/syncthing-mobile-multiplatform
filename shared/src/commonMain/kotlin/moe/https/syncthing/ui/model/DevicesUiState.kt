package moe.https.syncthing.ui.model

import moe.https.syncthing.core.DevicesSnapshot
import moe.https.syncthing.core.SyncthingDevice
import moe.https.syncthing.core.SyncthingLocalInfo
import moe.https.syncthing.core.SyncthingPendingDevice

data class DevicesUiState(
    val devices: List<SyncthingDevice> = emptyList(),
    val pendingDevices: List<SyncthingPendingDevice> = emptyList(),
    val localInfo: SyncthingLocalInfo? = null,
    val isLoading: Boolean = false,
    val isPendingDeviceActionInProgress: Boolean = false,
    val hasLoaded: Boolean = false,
    // Kept for existing callers; operation failures are separate from loadError.
    val errorMessage: String? = null,
    val isPausingAllDevices: Boolean = false,
    val isResumingAllDevices: Boolean = false,
    val loadError: String? = null,
) {
    val showPauseAllDevices: Boolean
        get() = devices.any { !it.paused }

    val showResumeAllDevices: Boolean
        get() = devices.any { it.paused }

    val canPauseAllDevices: Boolean
        get() = showPauseAllDevices && !isLoading && !isPausingAllDevices && !isResumingAllDevices

    val canResumeAllDevices: Boolean
        get() = showResumeAllDevices && !isLoading && !isPausingAllDevices && !isResumingAllDevices
}

fun DevicesUiState.updateFrom(snapshot: DevicesSnapshot): DevicesUiState = copy(
    devices = snapshot.devices,
    pendingDevices = snapshot.pendingDevices,
    localInfo = snapshot.localInfo,
    isLoading = false,
    isPendingDeviceActionInProgress = false,
    hasLoaded = true,
    errorMessage = null,
    loadError = null,
)
