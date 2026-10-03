package moe.https.syncthing.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.core.NewDeviceConfiguration
import moe.https.syncthing.core.SyncthingDevice
import moe.https.syncthing.core.SyncthingDiscoveryStatus
import moe.https.syncthing.core.SyncthingListenAddress
import moe.https.syncthing.core.SyncthingPendingDevice
import moe.https.syncthing.core.displayColor
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.common_action_add
import moe.https.syncthing.generated.resources.common_action_block
import moe.https.syncthing.generated.resources.common_action_cancel
import moe.https.syncthing.generated.resources.common_action_close
import moe.https.syncthing.generated.resources.common_action_confirm
import moe.https.syncthing.generated.resources.common_action_delete
import moe.https.syncthing.generated.resources.common_action_edit
import moe.https.syncthing.generated.resources.common_action_ignore
import moe.https.syncthing.generated.resources.common_action_pause
import moe.https.syncthing.generated.resources.common_action_resume
import moe.https.syncthing.generated.resources.common_action_save
import moe.https.syncthing.generated.resources.common_connection
import moe.https.syncthing.generated.resources.common_core_not_running
import moe.https.syncthing.generated.resources.common_device_name
import moe.https.syncthing.generated.resources.common_download_limit_kib
import moe.https.syncthing.generated.resources.common_label_device
import moe.https.syncthing.generated.resources.common_label_device_discovery
import moe.https.syncthing.generated.resources.common_label_listen_addresses
import moe.https.syncthing.generated.resources.common_not_connected
import moe.https.syncthing.generated.resources.common_optional
import moe.https.syncthing.generated.resources.common_pull_to_refresh
import moe.https.syncthing.generated.resources.common_read_failed
import moe.https.syncthing.generated.resources.common_release_to_refresh
import moe.https.syncthing.generated.resources.common_required
import moe.https.syncthing.generated.resources.common_ungrouped
import moe.https.syncthing.generated.resources.common_unlimited
import moe.https.syncthing.generated.resources.common_upload_limit_kib
import moe.https.syncthing.generated.resources.device_action_add_device
import moe.https.syncthing.generated.resources.device_action_edit_device
import moe.https.syncthing.generated.resources.device_action_scan_qr_code
import moe.https.syncthing.generated.resources.device_address
import moe.https.syncthing.generated.resources.device_auto_accept
import moe.https.syncthing.generated.resources.device_auto_accept_summary
import moe.https.syncthing.generated.resources.device_compression
import moe.https.syncthing.generated.resources.device_compression_all
import moe.https.syncthing.generated.resources.device_compression_metadata
import moe.https.syncthing.generated.resources.device_compression_summary
import moe.https.syncthing.generated.resources.device_connection_count
import moe.https.syncthing.generated.resources.device_delete_device
import moe.https.syncthing.generated.resources.device_delete_device_confirmation
import moe.https.syncthing.generated.resources.device_discovery_none_enabled
import moe.https.syncthing.generated.resources.device_discovery_none_enabled_message
import moe.https.syncthing.generated.resources.device_empty_message
import moe.https.syncthing.generated.resources.device_empty_title
import moe.https.syncthing.generated.resources.device_group
import moe.https.syncthing.generated.resources.device_introducer
import moe.https.syncthing.generated.resources.device_introducer_summary
import moe.https.syncthing.generated.resources.device_label_client
import moe.https.syncthing.generated.resources.device_label_configured_addresses
import moe.https.syncthing.generated.resources.device_label_connection_address
import moe.https.syncthing.generated.resources.device_label_current_address
import moe.https.syncthing.generated.resources.device_label_device_id
import moe.https.syncthing.generated.resources.device_label_discovered_address
import moe.https.syncthing.generated.resources.device_label_last_connection
import moe.https.syncthing.generated.resources.device_listen_none_enabled
import moe.https.syncthing.generated.resources.device_listen_none_enabled_message
import moe.https.syncthing.generated.resources.device_loading_message
import moe.https.syncthing.generated.resources.device_loading_title
import moe.https.syncthing.generated.resources.device_new
import moe.https.syncthing.generated.resources.device_new_device_group
import moe.https.syncthing.generated.resources.device_permissions
import moe.https.syncthing.generated.resources.device_requires_core
import moe.https.syncthing.generated.resources.device_state_connected
import moe.https.syncthing.generated.resources.device_unknown
import moe.https.syncthing.generated.resources.device_untrusted
import moe.https.syncthing.generated.resources.device_untrusted_summary
import moe.https.syncthing.ui.component.BlurredSmallTopAppBar
import moe.https.syncthing.ui.component.CheckableInputValueRow
import moe.https.syncthing.ui.component.CheckableValueRow
import moe.https.syncthing.ui.component.CoreNotReadyTakePlace
import moe.https.syncthing.ui.component.DeviceShareOverlayDialog
import moe.https.syncthing.ui.component.GroupedCard
import moe.https.syncthing.ui.component.InfoSwitch
import moe.https.syncthing.ui.component.InfoSwitchCard
import moe.https.syncthing.ui.component.InputValueRow
import moe.https.syncthing.ui.component.MultipleValueRow
import moe.https.syncthing.ui.component.PendingCard
import moe.https.syncthing.ui.component.barBackdropSource
import moe.https.syncthing.ui.model.DevicesUiState
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.util.countToColouredString
import moe.https.syncthing.ui.util.toReadable
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun DevicesScreen(
    uiState: DevicesUiState,
    coreState: CoreState,
    topAppBarScrollBehavior: ScrollBehavior,
    onRefresh: () -> Unit,
    onAddPendingDevice: (SyncthingPendingDevice) -> Unit,
    onDismissPendingDevice: (String) -> Unit,
    onIgnorePendingDevice: (SyncthingPendingDevice) -> Unit,
    onPauseDevice: (String) -> Unit,
    onEditDevice: (SyncthingDevice) -> Unit,
    modifier: Modifier = Modifier,
    uiPadding: PaddingValues,
    pagePaddingHorizontal: Dp,
) {

    val pullToRefreshState = rememberPullToRefreshState()

    if (coreState != CoreState.RUNNING) {
        CoreNotReadyTakePlace(
            title = stringResource(Res.string.common_core_not_running),
            message = stringResource(Res.string.device_requires_core),
        )
    } else if (uiState.isLoading && uiState.devices.isEmpty() && uiState.pendingDevices.isEmpty()) {
        CoreNotReadyTakePlace(
            title = stringResource(Res.string.device_loading_title),
            message = stringResource(Res.string.device_loading_message),
        )
    } else if (uiState.errorMessage != null) {
        CoreNotReadyTakePlace(
            title = stringResource(Res.string.common_read_failed),
            message = uiState.errorMessage,
            isError = true,
        )
    } else if (uiState.hasLoaded && uiState.devices.isEmpty() && uiState.pendingDevices.isEmpty()) {
        CoreNotReadyTakePlace(
            title = stringResource(Res.string.device_empty_title),
            message = stringResource(Res.string.device_empty_message),
        )
    } else {
        PullToRefresh(
            modifier = Modifier.padding(top = uiPadding.calculateTopPadding()),
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            pullToRefreshState = pullToRefreshState,
            topAppBarScrollBehavior = topAppBarScrollBehavior,
            refreshTexts = listOf(stringResource(Res.string.common_pull_to_refresh), stringResource(Res.string.common_release_to_refresh)),
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = uiPadding.calculateBottomPadding())
                    .padding(horizontal = pagePaddingHorizontal, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.pendingDevices.forEach { device ->
                    key("pending:${device.id}") {
                        NewDeviceCard(
                            device = device,
                            enabled = !uiState.isPendingDeviceActionInProgress,
                            onAdd = { onAddPendingDevice(device) },
                            onDismiss = { onDismissPendingDevice(device.id) },
                            onIgnore = { onIgnorePendingDevice(device) },
                        )
                    }
                }
                uiState.devices.filter { it.isLocal }.forEach { device ->
                    key(device.id) {
                        DeviceCard(
                            device,
                            defaultShowContentStatus = true,
                            deviceConnected = null,
                        ) { onShowShareOverlay ->
                            val (discoveryText, discoveryColor) = countToColouredString(
                                succeeded = uiState.localInfo?.discoveryStatus?.count { it.error == null } ?: 0,
                                total = uiState.localInfo?.discoveryStatus?.count() ?: 0,
                            )
                            val (listenText, listenColor) = countToColouredString(
                                uiState.localInfo?.listenAddresses?.count { it.error == null } ?: 0,
                                uiState.localInfo?.listenAddresses?.count() ?: 0,
                            )
                            var showDiscoveryOverlay by rememberSaveable { mutableStateOf(false) }
                            var showListenOverlay by rememberSaveable { mutableStateOf(false) }
                            var holdDown by rememberSaveable { mutableStateOf(false) }
                            val discoveryNoneEnabled = stringResource(Res.string.device_discovery_none_enabled)
                            val discoveryNoneEnabledMessage = stringResource(Res.string.device_discovery_none_enabled_message)
                            val listenNoneEnabled = stringResource(Res.string.device_listen_none_enabled)
                            val listenNoneEnabledMessage = stringResource(Res.string.device_listen_none_enabled_message)

                            MultipleValueRow(
                                label = stringResource(Res.string.device_label_device_id),
                                values = listOf(device.id.take(7)),
                                color = AppTheme.colorScheme.primary,
                                onClick = onShowShareOverlay,
                            )

                            MultipleValueRow(
                                label = stringResource(Res.string.common_label_device_discovery),
                                values = listOf(discoveryText),
                                color = discoveryColor,
                                onClick = { showDiscoveryOverlay = true },
                            )
                            MultipleValueRow(
                                label = stringResource(Res.string.common_label_listen_addresses),
                                values = listOf(listenText),
                                color = listenColor,
                                onClick = { showListenOverlay = true },
                            )

                            OverlayDialog(
                                show = showDiscoveryOverlay,
                                title = stringResource(Res.string.common_label_device_discovery),
                                onDismissRequest = { showDiscoveryOverlay = false },
                                onDismissFinished = { holdDown = false },
                                content = {
                                    Column ( horizontalAlignment = Alignment.CenterHorizontally ) {
                                        LazyColumn (
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 360.dp)
                                                .padding(vertical = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(
                                                uiState.localInfo?.discoveryStatus ?: listOf(
                                                    SyncthingDiscoveryStatus(
                                                        method = discoveryNoneEnabled,
                                                        error = discoveryNoneEnabledMessage,
                                                    )
                                                )
                                            ) { item ->
                                                if (item.error != null) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("●", color = AppTheme.colorScheme.error)
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(item.method)
                                                            Text(text = item.error.toCharArray().joinToString("\u200B"), color = AppTheme.colorScheme.onSecondaryContainer)
                                                        }
                                                    }
                                                } else {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("●", color = AppTheme.statusColors.ok)
                                                        Text(item.method, modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                        TextButton(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                                            text = stringResource(Res.string.common_action_confirm),
                                            onClick = { showDiscoveryOverlay = false },
                                            colors = textButtonColorsPrimary(),
                                        )
                                    }
                                }
                            )

                            OverlayDialog(
                                show = showListenOverlay,
                                title = stringResource(Res.string.common_label_listen_addresses),
                                onDismissRequest = { showListenOverlay = false },
                                onDismissFinished = { holdDown = false },
                                content = {
                                    Column (horizontalAlignment = Alignment.CenterHorizontally) {
                                        LazyColumn (
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 360.dp)
                                                .padding(vertical = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(
                                                uiState.localInfo?.listenAddresses ?: listOf (
                                                    SyncthingListenAddress(
                                                        address = listenNoneEnabled,
                                                        error = listenNoneEnabledMessage,
                                                    )
                                                )
                                            ) { item ->
                                                if (item.error != null) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("●", color = AppTheme.colorScheme.error)
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(item.address)
                                                            Text(text = item.error.toCharArray().joinToString("\u200B"), color = AppTheme.colorScheme.onSecondaryContainer)
                                                        }
                                                    }
                                                } else {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text("●", color = AppTheme.statusColors.ok)
                                                        Text(item.address, modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                        TextButton(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
                                            text = stringResource(Res.string.common_action_confirm),
                                            onClick = { showListenOverlay = false },
                                            colors = textButtonColorsPrimary(),
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                uiState.devices
                    .filterNot { it.isLocal }
                    .groupBy { it.group.trim() }
                    .toList()
                    .sortedWith(
                        compareBy<Pair<String, List<SyncthingDevice>>> { it.first.isBlank() }
                            .thenBy { it.first.lowercase() },
                    )
                    .forEach { (group, devices) ->
                        key("group:$group") {
                            GroupedCard (group) {
                                devices.forEach { device ->
                                    key(device.id) {
                                        DeviceCard(
                                            device,
                                            cornerRadius = CardDefaults.CornerRadius - 6.dp,
                                            deviceConnected = device.connected,
                                            cardColors = CardDefaults.defaultColors(
                                                color = AppTheme.colorScheme.secondaryContainer,
                                                contentColor = AppTheme.colorScheme.onSecondaryContainer,
                                            ),
                                        ) { onShowShareOverlay ->
                                            MultipleValueRow(
                                                label = stringResource(Res.string.device_label_device_id),
                                                values = listOf(device.id.take(7)),
                                                color = AppTheme.colorScheme.primary,
                                                onClick = onShowShareOverlay,
                                            )
                                            MultipleValueRow(
                                                label = stringResource(Res.string.device_label_current_address),
                                                values = listOf(
                                                    device.connectionAddress ?: "—"
                                                ),
                                            )
                                            MultipleValueRow(
                                                label = stringResource(Res.string.device_label_configured_addresses),
                                                values = listOf(
                                                    device.addresses.joinToString("、")
                                                        .ifBlank { "—" }),
                                            )
                                            MultipleValueRow(
                                                label = stringResource(Res.string.device_label_client),
                                                values = listOf(
                                                    device.clientVersion ?: "—"
                                                ),
                                            )

                                            device.lastConnectionAt?.let { lastConnectionAt ->
                                                MultipleValueRow(
                                                    label = stringResource(Res.string.device_label_last_connection),
                                                    values = listOf(lastConnectionAt.toReadable(stringResource(Res.string.common_not_connected))),
                                                )
                                            }
                                            if (device.discoveredAddresses.isNotEmpty()) {
                                                MultipleValueRow(
                                                    label = stringResource(Res.string.device_label_discovered_address),
                                                    values = device.discoveredAddresses,
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                TextButton(
                                                    modifier = Modifier.weight(1f),
                                                    text = stringResource(if (device.paused) Res.string.common_action_resume else Res.string.common_action_pause),
                                                    colors = textButtonColors(
                                                        color = AppTheme.colorScheme.surfaceContainerHigh,
                                                        textColor = AppTheme.colorScheme.onSurfaceContainer,
                                                    ),
                                                    onClick = { onPauseDevice(device.id) },
                                                )
                                                TextButton(
                                                    modifier = Modifier.weight(1f),
                                                    text = stringResource(Res.string.common_action_edit),
                                                    colors = textButtonColors(
                                                        color = AppTheme.colorScheme.surfaceContainerHigh,
                                                        textColor = AppTheme.colorScheme.onSurfaceContainer,
                                                    ),
                                                    onClick = { onEditDevice(device) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun NewDeviceCard(
    device: SyncthingPendingDevice,
    enabled: Boolean,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
) {
    PendingCard(
        title = stringResource(
            Res.string.device_new,
            device.name ?: device.address ?: stringResource(Res.string.device_unknown),
        ),
    ) {
        Column (
            modifier = Modifier.padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MultipleValueRow(
                label = stringResource(Res.string.device_label_device_id),
                values = listOf(device.id),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            MultipleValueRow(
                label = stringResource(Res.string.device_label_connection_address),
                values = listOf(device.address ?: "—"),
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = stringResource(Res.string.common_action_block),
                    enabled = enabled,
                    onClick = onIgnore,
                    colors = textButtonColors(
                        textColor = AppTheme.colorScheme.error,
                        borderColor = AppTheme.colorScheme.error,
                    )
                )
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = stringResource(Res.string.common_action_ignore),
                    enabled = enabled,
                    onClick = onDismiss,
                )
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = stringResource(Res.string.common_action_add),
                    enabled = enabled,
                    onClick = onAdd,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: SyncthingDevice,
    deviceConnected: Boolean?,
    cardColors: CardColors = CardDefaults.defaultColors(),
    defaultShowContentStatus: Boolean = false,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    content: @Composable ( onShowShareOverlay: (() -> Unit) ) -> Unit,
) {
    var holdDown by rememberSaveable { mutableStateOf(false) }
    var showShareOverlay by rememberSaveable { mutableStateOf(false) }
    var showContentStatus by rememberSaveable { mutableStateOf(defaultShowContentStatus) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = cornerRadius,
        colors = cardColors,
        holdDownState = holdDown,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { showContentStatus = !showContentStatus },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(0.7f),
                ) {
                    Text(
                        text = "●",
                        color = device.displayColor(),
                    )
                    Text(
                        text = device.name ?: stringResource(Res.string.device_unknown),
                        style = AppTheme.textStyles.headline1,
                        color = AppTheme.colorScheme.onBackground,
                    )
                }

                deviceConnected?.let {
                    Text(
                        text = stringResource(
                            if (deviceConnected) Res.string.device_state_connected else Res.string.common_not_connected,
                        ),
                        color = device.displayColor(),
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.3f),
                    )
                }
            }

            AnimatedVisibility(
                visible = showContentStatus,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column ( verticalArrangement = Arrangement.spacedBy(10.dp) ) {
                    HorizontalDivider()

                    content {
                        showShareOverlay = true
                    }
                }
            }
        }
    }

    DeviceShareOverlayDialog(
        show = showShareOverlay,
        onDismissRequest = { showShareOverlay = false },
        onDismissFinished = { holdDown = false },
        deviceID = device.id,
    )
}

@Composable
internal fun AddDeviceScreen(
    modifier: Modifier = Modifier,
    isSubmitting: Boolean,
    deviceGroups: List<String>,
    existingDevice: SyncthingDevice? = null,
    pendingDevice: SyncthingPendingDevice? = null,
    scannedDeviceId: String = "",
    onScanQrCode: () -> Unit,
    onConfirm: (NewDeviceConfiguration) -> Unit,
    navigateBack: () -> Unit,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
    onDeleteDevice: (String) -> Unit,
) {
    var deviceId by remember(existingDevice, pendingDevice, scannedDeviceId) {
        mutableStateOf(
            pendingDevice?.id
                ?: scannedDeviceId.takeIf(String::isNotBlank)
                ?: existingDevice?.id.orEmpty(),
        )
    }
    var name by remember(existingDevice, pendingDevice) {
        mutableStateOf(pendingDevice?.name ?: existingDevice?.name.orEmpty())
    }
    var group by remember(existingDevice) { mutableStateOf(existingDevice?.group.orEmpty()) }
    var addresses by remember(existingDevice) { mutableStateOf(existingDevice?.addresses?.joinToString(",").orEmpty()) }
    var introducer by remember(existingDevice) { mutableStateOf(existingDevice?.introducer ?: false) }
    var autoAcceptFolders by remember(existingDevice) { mutableStateOf(existingDevice?.autoAcceptFolders ?: false) }
    var compression by remember {
        mutableStateOf(existingDevice?.compression ?: NewDeviceConfiguration.Compression.METADATA)
    }
    var numConnections by remember(existingDevice) { mutableStateOf(existingDevice?.numConnections?.toString().orEmpty()) }
    var maxSendKiBPerSecond by remember(existingDevice) { mutableStateOf(existingDevice?.maxSendKiBPerSecond?.toString().orEmpty()) }
    var maxReceiveKiBPerSecond by remember(existingDevice) { mutableStateOf(existingDevice?.maxReceiveKiBPerSecond?.toString().orEmpty()) }
    var untrusted by remember(existingDevice) { mutableStateOf(existingDevice?.untrusted ?: false) }
    val numericValuesValid = listOf(
        numConnections,
        maxSendKiBPerSecond,
        maxReceiveKiBPerSecond,
    ).all { value -> (value.toIntOrNull()?:0) >= 0 }
    val canSubmit = deviceId.trim().isNotBlank() && numericValuesValid && !isSubmitting
    var holdDown by rememberSaveable { mutableStateOf(false) }
    var showDeleteOverlay by rememberSaveable { mutableStateOf(false) }
    var showDeviceGroupChooseSheet by rememberSaveable { mutableStateOf(false) }
    var chosenGroup by rememberSaveable(existingDevice) { mutableStateOf(group.trim()) }
    val availableDeviceGroups = remember(deviceGroups, group) {
        (deviceGroups + group)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sortedBy { it.lowercase() }
    }
    var newGroup by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = stringResource(
                if (existingDevice != null) Res.string.device_action_edit_device else Res.string.device_action_add_device,
            ),
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(Res.string.common_action_cancel),
                    )
                }
            },
            actions = {
                if ( existingDevice == null && pendingDevice == null ) {
                    IconButton(
                        onClick = {
                            onScanQrCode()
                        },
                        content = {
                            Icon(
                                contentDescription = stringResource(Res.string.device_action_scan_qr_code),
                                imageVector = MiuixIcons.Scan
                            )
                        },
                    )
                }
                IconButton(
                    enabled = canSubmit,
                    content = {
                        Icon(
                            contentDescription = stringResource(Res.string.common_action_save),
                            imageVector = MiuixIcons.Ok,
                            tint = if (canSubmit) {
                                AppTheme.colorScheme.onSurface
                            } else AppTheme.colorScheme.disabledOnSurface
                        )
                    },
                    onClick = {
                        onConfirm(
                            NewDeviceConfiguration(
                                deviceId = deviceId,
                                name = name,
                                group = group,
                                addresses = addresses
                                    .split(',', '\n')
                                    .map(String::trim)
                                    .filter(String::isNotBlank),
                                introducer = introducer,
                                autoAcceptFolders = autoAcceptFolders,
                                compression = compression,
                                numConnections = numConnections.toIntOrNull() ?: 0,
                                maxSendKiBPerSecond = maxSendKiBPerSecond.toIntOrNull() ?: 0,
                                maxReceiveKiBPerSecond = maxReceiveKiBPerSecond.toIntOrNull() ?: 0,
                                untrusted = untrusted,
                            ),
                        )
                    },
                )
            }
        ) },
        snackbarHost = {
            SnackbarHost(state = snackbarHostState)
        },
    ) { padding ->
        Box (
            modifier = Modifier
                .barBackdropSource(barBackdrop)
                .padding(horizontal = pagePaddingHorizontal)
                .nestedScroll(
                    scrollBehavior.nestedScrollConnection,
                )
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                InfoSwitchCard(
                    title = stringResource(Res.string.common_label_device),
                    content = {
                        InputValueRow(
                            value = deviceId,
                            onValueChange = { deviceId = it },
                            label = stringResource(Res.string.device_label_device_id),
                            labelWeight = 0.3f,
                            valueLabel = stringResource(Res.string.common_required),
                            singleLine = false,
                            allowEdit = existingDevice == null && pendingDevice == null
                        )

                        InputValueRow(
                            value = name,
                            onValueChange = { name = it },
                            label = stringResource(Res.string.common_device_name),
                            labelWeight = 0.3f,
                            valueLabel = stringResource(Res.string.common_optional),
                            singleLine = true,
                        )

                        ArrowPreference(
                            title = stringResource(Res.string.device_group),
                            enabled = !isSubmitting,
                            endActions = {
                                Text(
                                    if (group.isBlank()) stringResource(Res.string.common_ungrouped) else group.trim(),
                                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                                    color = if (!isSubmitting) MiuixTheme.colorScheme.onSurfaceVariantSummary else MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                                )
                            },
                            onClick = {
                                showDeviceGroupChooseSheet = true
                            },
                        )
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.device_permissions),
                    content = {
                        InfoSwitch(
                            title = stringResource(Res.string.device_introducer),
                            summary = stringResource(Res.string.device_introducer_summary),
                            enabled = !isSubmitting,
                            onCheckedChange = { introducer = !introducer },
                            checked = introducer,
                        )
                        InfoSwitch(
                            title = stringResource(Res.string.device_auto_accept),
                            summary = stringResource(Res.string.device_auto_accept_summary),
                            enabled = !isSubmitting,
                            onCheckedChange = { autoAcceptFolders = !autoAcceptFolders },
                            checked = autoAcceptFolders,
                        )
                        InfoSwitch(
                            title = stringResource(Res.string.device_untrusted),
                            summary = stringResource(Res.string.device_untrusted_summary),
                            enabled = !isSubmitting,
                            onCheckedChange = { untrusted = !untrusted },
                            checked = untrusted,
                        )
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.common_connection),
                    content = {
                        InputValueRow(
                            value = addresses,
                            onValueChange = { addresses = it },
                            label = stringResource(Res.string.device_address),
                            valueLabel = "dynamic",
                            singleLine = false,
                        )

                        InputValueRow(
                            value = maxSendKiBPerSecond,
                            onValueChange = { maxSendKiBPerSecond = it },
                            label = stringResource(Res.string.common_upload_limit_kib),
                            valueLabel = stringResource(Res.string.common_unlimited),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        InputValueRow(
                            value = maxReceiveKiBPerSecond,
                            onValueChange = { maxReceiveKiBPerSecond = it },
                            label = stringResource(Res.string.common_download_limit_kib),
                            valueLabel = stringResource(Res.string.common_unlimited),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        InputValueRow(
                            value = numConnections,
                            onValueChange = { numConnections = it },
                            label = stringResource(Res.string.device_connection_count),
                            valueLabel = "auto",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        WindowDropdownPreference(
                            title = stringResource(Res.string.device_compression),
                            summary = stringResource(Res.string.device_compression_summary),
                            items = listOf(
                                stringResource(Res.string.device_compression_all),
                                stringResource(Res.string.device_compression_metadata),
                                stringResource(Res.string.common_action_close),
                            ),
                            selectedIndex = compression.ordinal,
                            enabled = !isSubmitting,
                            onSelectedIndexChange = { selectedIndex ->
                                compression = NewDeviceConfiguration.Compression.entries[selectedIndex]
                            },
                        )
                    }
                )

                TextButton(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    text = stringResource(Res.string.common_action_delete),
                    onClick = { showDeleteOverlay = true },
                    colors = textButtonColors(
                        textColor = AppTheme.colorScheme.error,
                        borderColor = AppTheme.colorScheme.error,
                    )
                )
            }

            WindowDialog(
                title = stringResource(Res.string.device_group),
                show = showDeviceGroupChooseSheet,
                onDismissRequest = { showDeviceGroupChooseSheet = false },
                onDismissFinished = { showDeviceGroupChooseSheet = false },
            ) {
                Column (modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                    Card (
                        colors = CardDefaults.defaultColors(
                            color = AppTheme.colorScheme.secondaryContainer,
                            contentColor = AppTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        CheckableValueRow(
                            value = stringResource(Res.string.common_ungrouped),
                            state = chosenGroup.isBlank(),
                            dividerColor = AppTheme.colorScheme.onSurfaceContainerHigh,
                            onStateChange = { chosenGroup = "" },
                            checkBoxColorSet = CheckboxDefaults.checkboxColors(
                                uncheckedBackgroundColor = AppTheme.colorScheme.disabledOnSurface
                            ),
                        )
                        availableDeviceGroups.forEach { deviceGroup ->
                            key(deviceGroup) {
                                CheckableValueRow(
                                    value = deviceGroup,
                                    state = chosenGroup == deviceGroup,
                                    dividerColor = AppTheme.colorScheme.onSurfaceContainerHigh,
                                    onStateChange = { chosenGroup = deviceGroup },
                                    checkBoxColorSet = CheckboxDefaults.checkboxColors(
                                        uncheckedBackgroundColor = AppTheme.colorScheme.disabledOnSurface
                                    ),
                                )
                            }
                        }
                        CheckableInputValueRow(
                            state = chosenGroup == newGroup && chosenGroup != "",
                            value = newGroup,
                            valueLabel = stringResource(Res.string.device_new_device_group),
                            onValueChange = {
                                if (chosenGroup == newGroup && chosenGroup != "") {
                                    chosenGroup = it
                                }
                                newGroup = it
                            },
                            valueValidator = { it.isNotEmpty() && it !in availableDeviceGroups },
                            onStateChange = { chosenGroup = newGroup },
                            checkBoxColorSet = CheckboxDefaults.checkboxColors(
                                uncheckedBackgroundColor = AppTheme.colorScheme.disabledOnSurface
                            ),
                            showDivider = false,
                        )
                    }
                    Row (
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            text = stringResource(Res.string.common_action_cancel),
                            modifier = Modifier.weight(1f),
                            onClick = { showDeviceGroupChooseSheet = false },
                        )
                        TextButton(
                            text = stringResource(Res.string.common_action_confirm),
                            modifier = Modifier.weight(1f),
                            colors = textButtonColorsPrimary(),
                            onClick = {
                                group = chosenGroup
                                showDeviceGroupChooseSheet = false
                            },
                        )
                    }
                }
            }

            OverlayDialog(
                show = showDeleteOverlay,
                title = stringResource(Res.string.device_delete_device),
                onDismissRequest = { showDeleteOverlay = false },
                onDismissFinished = { holdDown = false },
            ) {
                Column {
                    Text(
                        text = stringResource(
                            Res.string.device_delete_device_confirmation,
                            name.toCharArray().joinToString("\u200B"),
                        ),
                        fontSize = 16.sp,
                        color = AppTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.common_action_cancel),
                            onClick = { showDeleteOverlay = false },
                        )
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.common_action_delete),
                            onClick = {
                                showDeleteOverlay = false
                                onDeleteDevice(deviceId)
                            },
                            colors = textButtonColors(
                                textColor = AppTheme.colorScheme.error,
                                borderColor = AppTheme.colorScheme.error,
                            ),
                        )
                    }
                }
            }
        }
    }
}
