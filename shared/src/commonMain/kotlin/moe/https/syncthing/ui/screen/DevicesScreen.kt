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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColors
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
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
            title = "核心未运行",
            message = "启动后才能读取设备连接状态。",
        )
    } else if (uiState.isLoading && uiState.devices.isEmpty() && uiState.pendingDevices.isEmpty()) {
        CoreNotReadyTakePlace(
            title = "正在读取设备",
            message = "正在获取设备列表…",
        )
    } else if (uiState.errorMessage != null) {
        CoreNotReadyTakePlace(
            title = "读取失败",
            message = uiState.errorMessage,
            isError = true,
        )
    } else if (uiState.hasLoaded && uiState.devices.isEmpty() && uiState.pendingDevices.isEmpty()) {
        CoreNotReadyTakePlace(
            title = "暂无设备",
            message = "当前还没有配置的设备。",
        )
    } else {
        PullToRefresh(
            modifier = Modifier.padding(top = uiPadding.calculateTopPadding()),
            isRefreshing = uiState.isLoading,
            onRefresh = onRefresh,
            pullToRefreshState = pullToRefreshState,
            topAppBarScrollBehavior = topAppBarScrollBehavior,
            refreshTexts = listOf("下拉刷新", "松手刷新"),
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

                            MultipleValueRow(
                                label = "设备 ID",
                                values = listOf(device.id.take(7)),
                                color = AppTheme.colorScheme.primary,
                                onClick = onShowShareOverlay,
                            )

                            MultipleValueRow(
                                label = "设备发现",
                                values = listOf(discoveryText),
                                color = discoveryColor,
                                onClick = { showDiscoveryOverlay = true },
                            )
                            MultipleValueRow(
                                label = "监听地址",
                                values = listOf(listenText),
                                color = listenColor,
                                onClick = { showListenOverlay = true },
                            )

                            OverlayDialog(
                                show = showDiscoveryOverlay,
                                title = "设备发现",
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
                                                        method = "无启用的设备发现",
                                                        error = "将仅连接到手动设置地址的设备。",
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
                                            text = "确定",
                                            onClick = { showDiscoveryOverlay = false },
                                            colors = textButtonColorsPrimary(),
                                        )
                                    }
                                }
                            )

                            OverlayDialog(
                                show = showListenOverlay,
                                title = "监听地址",
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
                                                        address = "无启用的监听地址",
                                                        error = "将仅能主动连接到其他设备。"
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
                                            text = "确定",
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
                                        ) { onShowShareOverlay ->
                                            MultipleValueRow(
                                                label = "设备 ID",
                                                values = listOf(device.id.take(7)),
                                                color = AppTheme.colorScheme.primary,
                                                onClick = onShowShareOverlay,
                                            )
                                            MultipleValueRow(
                                                label = "当前地址",
                                                values = listOf(
                                                    device.connectionAddress ?: "—"
                                                ),
                                            )
                                            MultipleValueRow(
                                                label = "配置地址",
                                                values = listOf(
                                                    device.addresses.joinToString("、")
                                                        .ifBlank { "—" }),
                                            )
                                            MultipleValueRow(
                                                label = "客户端",
                                                values = listOf(
                                                    device.clientVersion ?: "—"
                                                ),
                                            )

                                            device.lastConnectionAt?.let { lastConnectionAt ->
                                                MultipleValueRow(
                                                    label = "最后连接",
                                                    values = listOf(lastConnectionAt),
                                                )
                                            }
                                            if (device.discoveredAddresses.isNotEmpty()) {
                                                MultipleValueRow(
                                                    label = "发现地址",
                                                    values = device.discoveredAddresses,
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                TextButton(
                                                    modifier = Modifier.weight(1f),
                                                    text = if (device.paused) "恢复" else "暂停",
                                                    onClick = { onPauseDevice(device.id) },
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                TextButton(
                                                    modifier = Modifier.weight(1f),
                                                    text = "编辑",
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
    PendingCard(title = "新设备：${device.name ?: device.address ?: "未知设备"}") {
        Column (
            modifier = Modifier.padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MultipleValueRow(
                label = "设备 ID",
                values = listOf(device.id),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            MultipleValueRow(
                label = "连接地址",
                values = listOf(device.address ?: "—"),
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = "黑名单",
                    enabled = enabled,
                    onClick = onIgnore,
                    colors = textButtonColors(
                        textColor = AppTheme.colorScheme.error,
                        borderColor = AppTheme.colorScheme.error,
                    )
                )
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = "忽略",
                    enabled = enabled,
                    onClick = onDismiss,
                )
                TextButton(
                    modifier = Modifier.weight(0.3f),
                    text = "添加",
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
                        text = device.name ?: "未知设备",
                        style = AppTheme.textStyles.headline1,
                    )
                }

                deviceConnected?.let {
                    Text(
                        text = if (deviceConnected) "已连接" else "未连接",
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
            title = if ( existingDevice != null ) "编辑设备" else "添加设备",
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = "取消",
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
                                contentDescription = "扫描二维码",
                                imageVector = MiuixIcons.Scan
                            )
                        },
                    )
                }
                IconButton(
                    enabled = canSubmit,
                    content = {
                        Icon(
                            contentDescription = "保存",
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
                    title = "设备",
                    content = {
                        InputValueRow(
                            value = deviceId,
                            onValueChange = { deviceId = it },
                            label = "设备 ID",
                            labelWeight = 0.3f,
                            valueLabel = "必填",
                            singleLine = false,
                            allowEdit = existingDevice == null && pendingDevice == null
                        )

                        InputValueRow(
                            value = name,
                            onValueChange = { name = it },
                            label = "设备名",
                            labelWeight = 0.3f,
                            valueLabel = "选填",
                            singleLine = true,
                        )

                        ArrowPreference(
                            title = "设备组",
                            enabled = !isSubmitting,
                            endActions = {
                                Text(
                                    group.trim().ifBlank { "未分组" },
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
                    title = "权限",
                    content = {
                        InfoSwitch(
                            title = "作为中介",
                            summary = "将中介中的设备添加到我们的设备列表中，用于相互共享的文件夹。",
                            enabled = !isSubmitting,
                            onCheckedChange = { introducer = !introducer },
                            checked = introducer,
                        )
                        InfoSwitch(
                            title = "自动接受",
                            summary = "自动创建或共享此设备在默认路径上显示的文件夹。",
                            enabled = !isSubmitting,
                            onCheckedChange = { autoAcceptFolders = !autoAcceptFolders },
                            checked = autoAcceptFolders,
                        )
                        InfoSwitch(
                            title = "不受信任",
                            summary = "禁止与此设备共享未加密数据；共享文件夹必须配置加密密码。",
                            enabled = !isSubmitting,
                            onCheckedChange = { untrusted = !untrusted },
                            checked = untrusted,
                        )
                    }
                )

                InfoSwitchCard(
                    title = "连接",
                    content = {
                        InputValueRow(
                            value = addresses,
                            onValueChange = { addresses = it },
                            label = "地址",
                            valueLabel = "dynamic",
                            singleLine = false,
                        )

                        InputValueRow(
                            value = maxSendKiBPerSecond,
                            onValueChange = { maxSendKiBPerSecond = it },
                            label = "上传限速（KiB/s）",
                            valueLabel = "无限制",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        InputValueRow(
                            value = maxReceiveKiBPerSecond,
                            onValueChange = { maxReceiveKiBPerSecond = it },
                            label = "下载限速（KiB/s）",
                            valueLabel = "无限制",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        InputValueRow(
                            value = numConnections,
                            onValueChange = { numConnections = it },
                            label = "连接数",
                            valueLabel = "auto",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        WindowDropdownPreference(
                            title = "压缩",
                            summary = "选择与此设备通信时使用的压缩方式。",
                            items = listOf("所有", "仅元数据", "关闭"),
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
                    text = "删除",
                    onClick = { showDeleteOverlay = true },
                    colors = textButtonColors(
                        textColor = AppTheme.colorScheme.error,
                        borderColor = AppTheme.colorScheme.error,
                    )
                )
            }

            OverlayDialog(
                title = "设备组",
                show = showDeviceGroupChooseSheet,
                defaultWindowInsetsPadding = false,
                onDismissRequest = { showDeviceGroupChooseSheet = false },
                onDismissFinished = { showDeviceGroupChooseSheet = false },
            ) {
                Column (modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                    Card (
                        colors = CardColors(
                            color = AppTheme.colorScheme.surfaceContainerHigh,
                            contentColor = AppTheme.colorScheme.onSurfaceContainer,
                            borderColor = AppTheme.colorScheme.outline,
                        )
                    ) {
                        CheckableValueRow(
                            value = "未分组",
                            state = chosenGroup.isBlank(),
                            dividerColor = AppTheme.colorScheme.onSurfaceContainerVariant,
                            onStateChange = { chosenGroup = "" },
                        )
                        availableDeviceGroups.forEach { deviceGroup ->
                            key(deviceGroup) {
                                CheckableValueRow(
                                    value = deviceGroup,
                                    state = chosenGroup == deviceGroup,
                                    dividerColor = AppTheme.colorScheme.onSurfaceContainerVariant,
                                    onStateChange = { chosenGroup = deviceGroup },
                                )
                            }
                        }
                        CheckableInputValueRow(
                            state = chosenGroup == newGroup && chosenGroup != "",
                            value = newGroup,
                            valueLabel = "新建设备组",
                            onValueChange = {
                                if (chosenGroup == newGroup && chosenGroup != "") {
                                    chosenGroup = it
                                }
                                newGroup = it
                            },
                            valueValidator = { it.isNotEmpty() && it !in availableDeviceGroups },
                            onStateChange = { chosenGroup = newGroup },
                        )
                    }
                    Row (
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            text = "取消",
                            modifier = Modifier.weight(1f),
                            onClick = { showDeviceGroupChooseSheet = false },
                        )
                        TextButton(
                            text = "确定",
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
                title = "删除设备",
                onDismissRequest = { showDeleteOverlay = false },
                onDismissFinished = { holdDown = false },
            ) {
                Column {
                    Text(
                        text = "确定要删除设备 “${name.toCharArray().joinToString("\u200B")}” 吗？删除该设备不会删除从该设备同步的文件夹。",
                        fontSize = 16.sp,
                        color = AppTheme.colorScheme.onSurfaceVariantSummary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = "取消",
                            onClick = { showDeleteOverlay = false },
                        )
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = "删除",
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
