package moe.https.syncthing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.core.GuiTlsFile
import moe.https.syncthing.core.SyncthingDevice
import moe.https.syncthing.core.SyncthingFolder
import moe.https.syncthing.core.SyncthingPendingDevice
import moe.https.syncthing.core.SyncthingPendingFolder
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.about_page_about
import moe.https.syncthing.generated.resources.about_page_licenses
import moe.https.syncthing.generated.resources.common_action_back
import moe.https.syncthing.generated.resources.common_action_refresh
import moe.https.syncthing.generated.resources.common_advanced
import moe.https.syncthing.generated.resources.common_label_device
import moe.https.syncthing.generated.resources.common_label_folder
import moe.https.syncthing.generated.resources.common_save_failed
import moe.https.syncthing.generated.resources.core_page_home
import moe.https.syncthing.generated.resources.device_action_add_device
import moe.https.syncthing.generated.resources.device_page_connections
import moe.https.syncthing.generated.resources.folder_action_add_folder
import moe.https.syncthing.generated.resources.folder_page_folders
import moe.https.syncthing.generated.resources.recent_page_recent_changes
import moe.https.syncthing.generated.resources.setting_action_restart_core
import moe.https.syncthing.generated.resources.setting_https_certificate
import moe.https.syncthing.generated.resources.setting_https_private_key
import moe.https.syncthing.generated.resources.setting_page_appearance
import moe.https.syncthing.generated.resources.setting_page_autostart
import moe.https.syncthing.generated.resources.setting_page_background_running
import moe.https.syncthing.generated.resources.setting_page_backup
import moe.https.syncthing.generated.resources.setting_page_battery_conditions
import moe.https.syncthing.generated.resources.setting_page_common
import moe.https.syncthing.generated.resources.setting_page_connection
import moe.https.syncthing.generated.resources.setting_page_core_management
import moe.https.syncthing.generated.resources.setting_page_debug
import moe.https.syncthing.generated.resources.setting_page_developer_settings
import moe.https.syncthing.generated.resources.setting_page_discovery_servers
import moe.https.syncthing.generated.resources.setting_page_listen_addresses
import moe.https.syncthing.generated.resources.setting_page_local_network_permission
import moe.https.syncthing.generated.resources.setting_page_location_permission
import moe.https.syncthing.generated.resources.setting_page_network_conditions
import moe.https.syncthing.generated.resources.setting_page_permissions
import moe.https.syncthing.generated.resources.setting_page_settings
import moe.https.syncthing.generated.resources.setting_page_storage
import moe.https.syncthing.generated.resources.setting_page_storage_permission
import moe.https.syncthing.generated.resources.setting_page_time_ranges
import moe.https.syncthing.generated.resources.setting_page_webui
import moe.https.syncthing.generated.resources.setting_saved_restart_required
import moe.https.syncthing.generated.resources.setting_tls_file_selected
import moe.https.syncthing.generated.resources.webui_page_webui
import moe.https.syncthing.ui.component.AdaptiveTopAppBar
import moe.https.syncthing.ui.component.AppNavigationBar
import moe.https.syncthing.ui.component.BlurredSmallTopAppBar
import moe.https.syncthing.ui.component.barBackdropSource
import moe.https.syncthing.ui.component.rememberBarBackdrop
import moe.https.syncthing.ui.model.AppPage
import moe.https.syncthing.ui.model.MainUiState
import moe.https.syncthing.ui.screen.AboutScreen
import moe.https.syncthing.ui.screen.CoreScreen
import moe.https.syncthing.ui.screen.DevSettingPage
import moe.https.syncthing.ui.screen.DevicesScreen
import moe.https.syncthing.ui.screen.EditDeviceScreen
import moe.https.syncthing.ui.screen.EditFolderScreen
import moe.https.syncthing.ui.screen.FoldersScreen
import moe.https.syncthing.ui.screen.LicenceScreen
import moe.https.syncthing.ui.screen.LogScreen
import moe.https.syncthing.ui.screen.RecentChangesScreen
import moe.https.syncthing.ui.screen.SettingAutoStartScreen
import moe.https.syncthing.ui.screen.SettingBackgroundRunningAdvancedPage
import moe.https.syncthing.ui.screen.SettingBackgroundRunningBatteryPage
import moe.https.syncthing.ui.screen.SettingBackgroundRunningDurationPage
import moe.https.syncthing.ui.screen.SettingBackgroundRunningNetworkPage
import moe.https.syncthing.ui.screen.SettingBackgroundRunningPage
import moe.https.syncthing.ui.screen.SettingBackupPage
import moe.https.syncthing.ui.screen.SettingCommonScreen
import moe.https.syncthing.ui.screen.SettingConnectionScreen
import moe.https.syncthing.ui.screen.SettingCoreSelectScreen
import moe.https.syncthing.ui.screen.SettingEditDiscoveryScreen
import moe.https.syncthing.ui.screen.SettingEditListenScreen
import moe.https.syncthing.ui.screen.SettingLocalNetworkPermissionPage
import moe.https.syncthing.ui.screen.SettingPermissionPage
import moe.https.syncthing.ui.screen.SettingPositionPermissionPage
import moe.https.syncthing.ui.screen.SettingScreen
import moe.https.syncthing.ui.screen.SettingStoragePermissionPage
import moe.https.syncthing.ui.screen.SettingStorageScreen
import moe.https.syncthing.ui.screen.SettingThemePage
import moe.https.syncthing.ui.screen.SettingWebuiScreen
import moe.https.syncthing.ui.screen.WebviewScreen
import moe.https.syncthing.ui.theme.Add
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.theme.AppThemeController
import moe.https.syncthing.ui.theme.ArrowLeft
import moe.https.syncthing.ui.theme.ColorSchemeMode
import moe.https.syncthing.ui.theme.Device
import moe.https.syncthing.ui.theme.Earth4Filled
import moe.https.syncthing.ui.theme.FileDownload
import moe.https.syncthing.ui.theme.Folder
import moe.https.syncthing.ui.theme.Home4
import moe.https.syncthing.ui.theme.MingCuteIcons
import moe.https.syncthing.ui.theme.Refresh
import moe.https.syncthing.ui.theme.Settings
import moe.https.syncthing.ui.util.localizedTitle
import moe.https.syncthing.ui.util.withPageBottomSpacing
import moe.https.syncthing.viewmodel.BackupViewModel
import moe.https.syncthing.viewmodel.CoreViewModel
import moe.https.syncthing.viewmodel.DevicesViewModel
import moe.https.syncthing.viewmodel.FoldersViewModel
import moe.https.syncthing.viewmodel.LogViewModel
import moe.https.syncthing.viewmodel.MainViewModel
import moe.https.syncthing.viewmodel.RecentChangesViewModel
import moe.https.syncthing.viewmodel.SettingViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.rememberNavController
import top.yukonga.miuix.kmp.nav.gesture.PredictiveBackHandler
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection

@Composable
fun App(
    coreViewModel: CoreViewModel,
    logViewModel: LogViewModel,
    devicesViewModel: DevicesViewModel,
    foldersViewModel: FoldersViewModel,
    recentChangesViewModel: RecentChangesViewModel,
    settingViewModel: SettingViewModel,
    mainViewModel: MainViewModel,
    backupViewModel: BackupViewModel,
    versionName: String,
    developerModeEnabled: Boolean,
    onModifyDeveloperMode: () -> Unit,
    onScanQrCode: () -> Unit,
    publicStorageAccessGranted: Boolean,
    onRequestPublicStorageAccess: () -> Unit,
    localNetworkPermissionRequired: Boolean,
    localNetworkAccessGranted: Boolean,
    onRequestLocalNetworkAccess: () -> Unit,
    currentWifiName: String?,
    wifiNameAccessGranted: Boolean,
    locationServiceEnabled: Boolean,
    onRequestWifiNameAccess: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    batteryOptimizationExempt: Boolean,
    onBatteryOptimizationRequest: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    scannedDeviceId: String,
    webUiUrlProvider: () -> String,
    webView: @Composable (
        url: String,
        reloadToken: Int,
        onScroll: (deltaY: Float, isAtTop: Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
) {
    val coreUiState by coreViewModel.uiState.collectAsState()
    val logUiState by logViewModel.uiState.collectAsState()
    val devicesUiState by devicesViewModel.uiState.collectAsState()
    val foldersUiState by foldersViewModel.uiState.collectAsState()
    val recentChangesUiState by recentChangesViewModel.uiState.collectAsState()
    val settingUiState by settingViewModel.uiState.collectAsState()
    val mainUiState by mainViewModel.uiState.collectAsState()
    val backupUiState by backupViewModel.uiState.collectAsState()
    val windowWidthDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp().value.toInt()
    }
    val bottomBarCapacity = MainUiState.calculateBottomBarCapacity(windowWidthDp)
    val visibleBottomBarPages = MainUiState.visibleBottomBarPages(
        selectedPages = mainUiState.bottomBarPages,
        capacity = bottomBarCapacity,
    )
    val initialMainPage = remember { mainUiState.defaultBottomBarPage }
    var currentPageMain by remember { mutableStateOf(initialMainPage) }
    var requestedPageMain by remember { mutableStateOf<AppPage?>(null) }
    var initialMainPageRefreshRequested by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<SyncthingDevice?>(null) }
    var pendingDeviceToAdd by remember { mutableStateOf<SyncthingPendingDevice?>(null) }
    var editingFolder by remember { mutableStateOf<SyncthingFolder?>(null) }
    var pendingFolderToAdd by remember { mutableStateOf<SyncthingPendingFolder?>(null) }
    var selectedFolderPath by remember { mutableStateOf<String?>(null) }
    var folderPathChooserFolderId by remember { mutableStateOf<String?>(null) }
    var webUiReloadToken by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val plainPageSnackbarHostState = remember { SnackbarHostState() }
    val settingSnackbarHostState = remember { SnackbarHostState() }
    val restartSnackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController<AppRoute>(AppRoute.Main)
    val mainScrollBehavior = MiuixScrollBehavior()
    val plainScrollBehavior = MiuixScrollBehavior()

    LaunchedEffect(windowWidthDp) {
        if (windowWidthDp > 0) {
            mainViewModel.onBottomBarCapacityChanged(bottomBarCapacity)
        }
    }

    val controller = remember {
        AppThemeController(
            colorSchemeMode = ColorSchemeMode.System,
            isHighContrast = mainUiState.highContrastMode,
        )
    }

    fun navigateTo(page: AppSubPage) {
        navController.push(AppRoute.Plain(page))
    }

    DisposableEffect(currentPageMain) {
        logViewModel.onPageVisibilityChanged(false)
        onDispose { }
    }

    LaunchedEffect(initialMainPage, currentPageMain, coreUiState.state) {
        if (initialMainPageRefreshRequested) return@LaunchedEffect
        if (currentPageMain != initialMainPage) {
            initialMainPageRefreshRequested = true
            return@LaunchedEffect
        }

        val canRefresh = when (initialMainPage) {
            AppPage.DEVICES,
            AppPage.FOLDERS,
            AppPage.RECENT_CHANGES -> coreUiState.state == CoreState.RUNNING
            else -> true
        }
        if (!canRefresh) return@LaunchedEffect

        initialMainPageRefreshRequested = true
        when (initialMainPage) {
            AppPage.DEVICES -> devicesViewModel.refresh()
            AppPage.FOLDERS -> foldersViewModel.refresh()
            AppPage.RECENT_CHANGES -> recentChangesViewModel.refresh()
            AppPage.SETTINGS -> settingViewModel.refresh()
            else -> Unit
        }
    }

    LaunchedEffect(
        requestedPageMain,
        visibleBottomBarPages,
        coreUiState.state,
        devicesUiState.isLoading,
        foldersUiState.isLoading,
        recentChangesUiState.isLoading,
        settingUiState.isLoading,
        logUiState.isLoading,
    ) {
        // A page switch is a one-shot request: it is dropped as soon as it is dispatched, so later
        // loading or core-state changes cannot replay the same navigation.
        val targetPage = requestedPageMain
        if (targetPage != null) {
            val ready = when (targetPage) {
                AppPage.DEVICES -> !devicesUiState.isLoading
                AppPage.FOLDERS -> !foldersUiState.isLoading
                AppPage.RECENT_CHANGES -> !recentChangesUiState.isLoading
                AppPage.SETTINGS -> !settingUiState.isLoading
                else -> true
            }

            if (ready) {
                requestedPageMain = null
                if (targetPage in visibleBottomBarPages) {
                    currentPageMain = targetPage
                } else {
                    navigateTo(targetPage.plainSubPage())
                }
            }
        }

        if (coreUiState.state != CoreState.RUNNING) {
            settingViewModel.onCoreUnavailable()
        }
    }

    LaunchedEffect(settingViewModel) {
        settingViewModel.saveErrors.collect { message ->
            settingSnackbarHostState.showSnackbar(
                getString(Res.string.common_save_failed, message),
            )
        }
    }

    val restartMessage = stringResource(Res.string.setting_saved_restart_required)
    val restartActionLabel = stringResource(Res.string.setting_action_restart_core)
    LaunchedEffect(settingUiState.showRestartPrompt, coreUiState.state) {
        if (!settingUiState.showRestartPrompt || coreUiState.state != CoreState.RUNNING) {
            return@LaunchedEffect
        }
        val result = coroutineScope {
            val pendingResult = async(start = CoroutineStart.UNDISPATCHED) {
                restartSnackbarHostState.showSnackbar(
                    message = restartMessage,
                    actionLabel = restartActionLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
            }
            val snackbar = restartSnackbarHostState.newestSnackbarData()
            try {
                pendingResult.await()
            } finally {
                // The host retains canceled requests; dismiss this action when its effect ends.
                withContext(NonCancellable) { snackbar?.dismiss() }
            }
        }
        settingViewModel.onRestartPromptDismissed()
        if (result == SnackbarResult.ActionPerformed) settingViewModel.restartCore()
    }

    val selectedTlsFile = settingUiState.selectedGuiTlsFile
    val tlsSelectionMessage = if (selectedTlsFile != null) {
        val name = stringResource(
            if (selectedTlsFile == GuiTlsFile.CERTIFICATE) Res.string.setting_https_certificate else Res.string.setting_https_private_key,
        )
        stringResource(Res.string.setting_tls_file_selected, name)
    } else null
    LaunchedEffect(selectedTlsFile) {
        if (!tlsSelectionMessage.isNullOrBlank()) {
            settingSnackbarHostState.showSnackbar(tlsSelectionMessage)
            settingViewModel.onNoticeMessageShown()
        }
    }

    fun requestSwitchToPageMain( targetPage: AppPage ) {
        // Dispatching the switch is left to the effect below: it waits for the target page data and
        // opens a plain page when the target is not visible in the bottom bar.
        requestedPageMain = targetPage

        when (targetPage) {
            AppPage.DEVICES -> devicesViewModel.refresh()
            AppPage.FOLDERS -> foldersViewModel.refresh()
            AppPage.RECENT_CHANGES -> recentChangesViewModel.refresh()
            AppPage.SETTINGS -> settingViewModel.refresh()
            AppPage.CORE, AppPage.WEBUI -> Unit
        }
    }

    AppTheme(
        controller = controller,
    ) {
        val swipeBackDirection = when (LocalLayoutDirection.current) {
            LayoutDirection.Ltr -> NavSwipeDirection.LeftToRight
            LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
        }

        val pagePaddingHorizontal = 20.dp
        val anyBarBlurEnabled = mainUiState.topBarBlurEnabled || mainUiState.bottomBarBlurEnabled
        val mainBarBackdrop = rememberBarBackdrop(
            enabled = anyBarBlurEnabled && currentPageMain != AppPage.WEBUI,
        )

        PredictiveBackHandler(
            enabled = navController.backStack.size == 1 &&
                currentPageMain != mainUiState.defaultBottomBarPage,
            onProgress = {},
            onCommit = {
                requestSwitchToPageMain(mainUiState.defaultBottomBarPage)
            },
            onCancel = {},
        )

        NavDisplay(
            navController = navController,
            effects = NavDisplayEffects(blockInputDuringTransition = true),
        ) {
            entry<AppRoute.Main> {
                Scaffold(
                    topBar = {
                        AnimatedContent(
                            targetState = currentPageMain,
                            transitionSpec = { fadeIn(tween(durationMillis = 160)) togetherWith fadeOut(tween(durationMillis = 160)) },
                            label = "MainTopAppBarTransition",
                        ) { page ->
                            AdaptiveTopAppBar(
                                title = page.localizedTitle(),
                                showTopAppBar = true,
                                scrollBehavior = mainScrollBehavior,
                                backdrop = mainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                                actions = {
                                    if (page == AppPage.DEVICES && coreUiState.state == CoreState.RUNNING) {
                                        IconButton(
                                            onClick = {
                                                editingDevice = null
                                                pendingDeviceToAdd = null
                                                navigateTo(AppSubPage.DEVICE_ADD)
                                            },
                                            content = {
                                                Icon(
                                                    contentDescription = stringResource(Res.string.device_action_add_device),
                                                    imageVector = MingCuteIcons.Add
                                                )
                                            },
                                        )
                                    }
                                    if (page == AppPage.WEBUI && coreUiState.state == CoreState.RUNNING) {
                                        IconButton(
                                            onClick = { webUiReloadToken += 1 },
                                            content = {
                                                Icon(
                                                    contentDescription = stringResource(Res.string.common_action_refresh),
                                                    imageVector = MingCuteIcons.Refresh,
                                                )
                                            },
                                        )
                                    }
                                    if (page == AppPage.FOLDERS && coreUiState.state == CoreState.RUNNING) {
                                        IconButton(
                                            onClick = {
                                                editingFolder = null
                                                pendingFolderToAdd = null
                                                selectedFolderPath = null
                                                folderPathChooserFolderId = null
                                                devicesViewModel.refresh()
                                                navigateTo(AppSubPage.FOLDER_ADD)
                                            },
                                            content = {
                                                Icon(
                                                    contentDescription = stringResource(Res.string.folder_action_add_folder),
                                                    imageVector = MingCuteIcons.Add,
                                                )
                                            },
                                        )
                                    }
                                },
                                isWideScreen = false,
                            )
                        }
                    },
                    bottomBar = {
                        AppNavigationBar(
                            entries = AppPage.entries,
                            visiblePages = visibleBottomBarPages,
                            currentPage = currentPageMain,
                            onNavigationBarItemClick = ::requestSwitchToPageMain,
                            floating = mainUiState.floatingBottomBar,
                            backdrop = mainBarBackdrop.takeIf { mainUiState.bottomBarBlurEnabled },
                        )
                    },
                    snackbarHost = {
                        Column {
                            SnackbarHost(state = snackbarHostState)
                            SnackbarHost(state = settingSnackbarHostState)
                            SnackbarHost(state = restartSnackbarHostState)
                        }
                    },
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .barBackdropSource(mainBarBackdrop)
                            .nestedScroll(mainScrollBehavior.nestedScrollConnection),
                    ) {
                        AnimatedContent(
                            targetState = currentPageMain,
                            transitionSpec = {
                                if (targetState.ordinal > initialState.ordinal) {
                                    (slideInHorizontally { it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "MainPageTransition",
                        ) { page ->
                            val uiPadding = PaddingValues(
                                top = padding.calculateTopPadding(),
                                bottom = padding.calculateBottomPadding()
                            )

                            when (page) {
                                AppPage.DEVICES -> DevicesScreen(
                                    uiPadding = uiPadding,
                                    pagePaddingHorizontal = pagePaddingHorizontal,
                                    uiState = devicesUiState,
                                    coreState = coreUiState.state,
                                    topAppBarScrollBehavior = mainScrollBehavior,
                                    onRefresh = devicesViewModel::refresh,
                                    onAddPendingDevice = { device ->
                                        editingDevice = null
                                        pendingDeviceToAdd = device
                                        navigateTo(AppSubPage.DEVICE_ADD)
                                    },
                                    onDismissPendingDevice = devicesViewModel::dismissPendingDevice,
                                    onIgnorePendingDevice = devicesViewModel::ignorePendingDevice,
                                    onPauseDevice = devicesViewModel::pauseDevice,
                                    onEditDevice = { device ->
                                        editingDevice = device
                                        pendingDeviceToAdd = null
                                        navigateTo(AppSubPage.DEVICE_ADD)
                                    },
                                )

                                AppPage.FOLDERS -> FoldersScreen(
                                    uiPadding = uiPadding,
                                    pagePaddingHorizontal = pagePaddingHorizontal,
                                    uiState = foldersUiState,
                                    coreState = coreUiState.state,
                                    topAppBarScrollBehavior = mainScrollBehavior,
                                    onRefresh = foldersViewModel::refresh,
                                    onAddPendingFolder = { folder ->
                                        editingFolder = null
                                        pendingFolderToAdd = folder
                                        selectedFolderPath = null
                                        folderPathChooserFolderId = null
                                        devicesViewModel.refresh()
                                        navigateTo(AppSubPage.FOLDER_ADD)
                                    },
                                    onDismissPendingFolder = foldersViewModel::dismissPendingFolder,
                                    onIgnorePendingFolder = foldersViewModel::ignorePendingFolder,
                                    onSetFolderPaused = foldersViewModel::setFolderPaused,
                                    onRescanFolder = foldersViewModel::rescanFolder,
                                    snackbarHostState = snackbarHostState,
                                    onEditFolder = { folder ->
                                        editingFolder = folder
                                        pendingFolderToAdd = null
                                        selectedFolderPath = folder.path
                                        folderPathChooserFolderId = null
                                        devicesViewModel.refresh()
                                        navigateTo(AppSubPage.FOLDER_ADD)
                                    },
                                )

                                AppPage.SETTINGS -> SettingScreen(
                                    uiPadding = uiPadding,
                                    pagePaddingHorizontal = pagePaddingHorizontal,
                                    uiState = settingUiState,
                                    developerModeEnabled = developerModeEnabled,
                                    onNavigateTo = ::navigateTo,
                                )

                                AppPage.CORE -> CoreScreen(
                                    uiPadding = uiPadding,
                                    pagePaddingHorizontal = pagePaddingHorizontal,
                                    uiState = coreUiState,
                                    onStartAction = if (coreUiState.isStarted) {
                                        coreViewModel::onStopClicked
                                    } else {
                                        coreViewModel::onStartClicked
                                    },
                                    visiblePages = visibleBottomBarPages,
                                    snackbarHostState = snackbarHostState,
                                    developerModeEnabled = developerModeEnabled,
                                    onModifyDeveloperMode = onModifyDeveloperMode,
                                    onNavigateTo = ::navigateTo,
                                    onSwitchTo = ::requestSwitchToPageMain,
                                )

                                AppPage.WEBUI -> WebviewScreen(
                                    uiPadding = uiPadding,
                                    coreState = coreUiState.state,
                                    topAppBarScrollBehavior = mainScrollBehavior,
                                    webUiUrl = if (coreUiState.state == CoreState.RUNNING) {
                                        webUiUrlProvider()
                                    } else {
                                        null
                                    },
                                    reloadToken = webUiReloadToken,
                                    webView = webView,
                                )

                                AppPage.RECENT_CHANGES -> RecentChangesScreen(
                                    uiPadding = uiPadding,
                                    pagePaddingHorizontal = pagePaddingHorizontal,
                                    uiState = recentChangesUiState,
                                    coreState = coreUiState.state,
                                    topAppBarScrollBehavior = mainScrollBehavior,
                                    onRefresh = recentChangesViewModel::refresh,
                                )
                            }
                        }
                    }
                }
            }

            entry<AppRoute.Plain>(swipeDismiss = swipeBackDirection) { route ->
                val currentPagePlain = route.page
                // The WebUI page hosts a real Android view; recording the page content into a backdrop
                // layer makes it blink on Android, so the blur is skipped for that page, exactly like
                // the main bar backdrop above.
                val plainBarBackdrop = rememberBarBackdrop(
                    enabled = anyBarBlurEnabled && currentPagePlain != AppSubPage.MAIN_NONEXIST_NAVIGATOR_WEBUI,
                )
                val navigateBack = {
                    if (currentPagePlain == AppSubPage.DEVICE_ADD) {
                        editingDevice = null
                        pendingDeviceToAdd = null
                    }
                    if (currentPagePlain == AppSubPage.FOLDER_ADD) {
                        editingFolder = null
                        pendingFolderToAdd = null
                        selectedFolderPath = null
                    }
                    if (currentPagePlain == AppSubPage.SETTINGS_STORAGE_PERMISSION) {
                        folderPathChooserFolderId = null
                    }
                    navController.pop()
                    Unit
                }

                when (currentPagePlain) {
                    AppSubPage.DEBUG -> {
                        LogScreen(
                            uiState = logUiState,
                            onSourceSelected = logViewModel::onSourceSelected,
                            navigateBack = navigateBack,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                        )
                    }
                    AppSubPage.SETTINGS_STORAGE_PERMISSION -> {
                        SettingStoragePermissionPage(
                            granted = publicStorageAccessGranted,
                            onRequestPermission = onRequestPublicStorageAccess,
                            folderId = folderPathChooserFolderId,
                            selectedFolderPath = selectedFolderPath,
                            onFolderPathSelected = { selectedFolderPath = it },
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                        )
                    }

                    AppSubPage.DEVICE_ADD -> {
                        EditDeviceScreen(
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            isSubmitting = devicesUiState.isLoading,
                            deviceGroups = devicesUiState.devices.map { it.group },
                            existingDevice = editingDevice,
                            pendingDevice = pendingDeviceToAdd,
                            scannedDeviceId = scannedDeviceId,
                            onScanQrCode = onScanQrCode,
                            onConfirm = { configuration ->
                                if (editingDevice == null) devicesViewModel.addDevice(
                                    configuration
                                )
                                else devicesViewModel.updateDevice(configuration)
                                editingDevice = null
                                pendingDeviceToAdd = null
                                navigateBack()
                            },
                            navigateBack = navigateBack,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                            onDeleteDevice = devicesViewModel::deleteDevice,
                        )
                    }

                    AppSubPage.FOLDER_ADD -> {
                        EditFolderScreen(
                            isSubmitting = foldersUiState.isLoading,
                            folderGroups = foldersUiState.folders.map { it.group },
                            actionError = foldersUiState.actionError,
                            devices = devicesUiState.devices,
                            existingFolder = editingFolder,
                            pendingFolder = pendingFolderToAdd,
                            selectedFolderPath = selectedFolderPath,
                            onConfirm = { configuration ->
                                if (editingFolder == null) foldersViewModel.addFolder(
                                    configuration
                                )
                                else foldersViewModel.updateFolder(configuration)
                                editingFolder = null
                                pendingFolderToAdd = null
                                selectedFolderPath = null
                                navigateBack()
                            },
                            onRedirectToPathChooserPage = { folderId ->
                                folderPathChooserFolderId = folderId
                                navigateTo(AppSubPage.SETTINGS_STORAGE_PERMISSION)
                            },
                            onDeleteFolder = { folderId, deleteLocalFiles ->
                                foldersViewModel.deleteFolder(
                                    folderId = folderId,
                                    deleteLocalFiles = deleteLocalFiles,
                                    onSuccess = navigateBack,
                                )
                            },
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                        )
                    }

                    AppSubPage.DEV -> {
                        DevSettingPage(
                            requestSwitchToPageMain = { appPage ->
                                requestSwitchToPageMain(appPage)
                                navController.popUntil { it is AppRoute.Main }
                            },
                            requestSwitchToPagePlain = { appSubPage ->
                                if (appSubPage != currentPagePlain) {
                                    navigateTo(appSubPage)
                                }
                            },
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                            developerModeEnabled = developerModeEnabled,
                            onModifyDeveloperMode = onModifyDeveloperMode,
                        )
                    }

                    AppSubPage.SETTINGS_BACKGROUND_RUNNING -> {
                        SettingBackgroundRunningPage(
                            batteryOptimizationExempt = batteryOptimizationExempt,
                            onBatteryOptimizationRequest = onBatteryOptimizationRequest,
                            onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                        )
                    }

                    AppSubPage.SETTINGS_THEME -> {
                        SettingThemePage(
                            uiState = mainUiState,
                            visibleBottomBarPages = visibleBottomBarPages,
                            onPageToggle = mainViewModel::onBottomBarPageToggled,
                            onDefaultPageChange = mainViewModel::onDefaultBottomBarPageSelected,
                            onFloatingBottomBarChange = mainViewModel::onFloatingBottomBarChanged,
                            onTopBarBlurChange = mainViewModel::onTopBarBlurChanged,
                            onBottomBarBlurChange = mainViewModel::onBottomBarBlurChanged,
                            onHighContrastModeChange = { enabled ->
                                controller.isHighContrast = enabled
                                mainViewModel.onHighContrastModeChanged(enabled)
                            },
                            navigateBack = navigateBack,
                            scrollBehavior = plainScrollBehavior,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop,
                        )
                    }

                    AppSubPage.SETTINGS_DISCOVERY_EDIT -> {
                        SettingEditDiscoveryScreen(
                            settingViewModel = settingViewModel,
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop,
                        )
                    }

                    AppSubPage.SETTINGS_LISTEN_EDIT -> {
                        SettingEditListenScreen(
                            settingViewModel = settingViewModel,
                            navigateBack = navigateBack,
                            pagePaddingHorizontal = pagePaddingHorizontal,
                            barBackdrop = plainBarBackdrop,
                        )
                    }

                    else -> {
                        Scaffold(
                            topBar = {
                                BlurredSmallTopAppBar(
                                    title = currentPagePlain.localizedTitle(),
                                    scrollBehavior = plainScrollBehavior,
                                    backdrop = plainBarBackdrop.takeIf { mainUiState.topBarBlurEnabled },
                                    navigationIcon = {
                                        IconButton(onClick = navigateBack) {
                                            Icon(
                                                imageVector = MingCuteIcons.ArrowLeft,
                                                contentDescription = stringResource(Res.string.common_action_back),
                                            )
                                        }
                                    },
                                    actions = {
                                        if (currentPagePlain == AppSubPage.MAIN_NONEXIST_NAVIGATOR_CONNECTION && coreUiState.state == CoreState.RUNNING) {
                                            IconButton(
                                                onClick = {
                                                    editingDevice = null
                                                    pendingDeviceToAdd = null
                                                    navigateTo(AppSubPage.DEVICE_ADD)
                                                },
                                                content = {
                                                    Icon(
                                                        contentDescription = stringResource(Res.string.device_action_add_device),
                                                        imageVector = MingCuteIcons.Add
                                                    )
                                                },
                                            )
                                        }
                                        if (currentPagePlain == AppSubPage.MAIN_NONEXIST_NAVIGATOR_WEBUI && coreUiState.state == CoreState.RUNNING) {
                                            IconButton(
                                                onClick = { webUiReloadToken += 1 },
                                                content = {
                                                    Icon(
                                                        contentDescription = stringResource(Res.string.common_action_refresh),
                                                        imageVector = MingCuteIcons.Refresh,
                                                    )
                                                },
                                            )
                                        }
                                        if (currentPagePlain == AppSubPage.MAIN_NONEXIST_NAVIGATOR_FOLDER && coreUiState.state == CoreState.RUNNING) {
                                            IconButton(
                                                onClick = {
                                                    editingFolder = null
                                                    pendingFolderToAdd = null
                                                    selectedFolderPath = null
                                                    folderPathChooserFolderId = null
                                                    devicesViewModel.refresh()
                                                    navigateTo(AppSubPage.FOLDER_ADD)
                                                },
                                                content = {
                                                    Icon(
                                                        contentDescription = stringResource(Res.string.folder_action_add_folder),
                                                        imageVector = MingCuteIcons.Add,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                )
                            },
                            snackbarHost = {
                                Column {
                                    SnackbarHost(state = plainPageSnackbarHostState)
                                    SnackbarHost(state = settingSnackbarHostState)
                                    SnackbarHost(state = restartSnackbarHostState)
                                }
                            },
                            containerColor = AppTheme.colorScheme.surface,
                        ) { padding ->
                            // Sub pages have no bottom bar, so the Scaffold inset alone would let
                            // their content end flush against the navigation bar.
                            val subPagePadding = padding.withPageBottomSpacing()
                            Box (
                                modifier = Modifier
                                    .barBackdropSource(plainBarBackdrop)
                                    .nestedScroll(
                                        plainScrollBehavior.nestedScrollConnection,
                                    )
                            ) {
                                when (currentPagePlain) {
                                    AppSubPage.ABOUT -> {
                                        AboutScreen(
                                            versionName = versionName,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.LICENCE -> {
                                        LicenceScreen(
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_CORE_MANAGE -> {
                                        SettingCoreSelectScreen(
                                            uiState = coreUiState,
                                            snackbarHostState = plainPageSnackbarHostState,
                                            onCoreSelected = coreViewModel::onCoreSelected,
                                            onImportCore = coreViewModel::onImportCoreClicked,
                                            onCoreDelete = coreViewModel::onCoreDelete,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_BACKGROUND_RUNNING_NETWORK -> {
                                        SettingBackgroundRunningNetworkPage(
                                            settingViewModel = settingViewModel,
                                            currentWifiName = currentWifiName,
                                            wifiNameAccessGranted = wifiNameAccessGranted,
                                            locationServiceEnabled = locationServiceEnabled,
                                            onRequestWifiNameAccess = onRequestWifiNameAccess,
                                            onOpenLocationSettings = onOpenLocationSettings,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_BACKGROUND_RUNNING_BATTERY -> {
                                        SettingBackgroundRunningBatteryPage(
                                            settingViewModel = settingViewModel,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_BACKGROUND_RUNNING_DURATION -> {
                                        SettingBackgroundRunningDurationPage(
                                            settingViewModel = settingViewModel,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_BACKGROUND_RUNNING_ADVANCED -> {
                                        SettingBackgroundRunningAdvancedPage(
                                            settingViewModel = settingViewModel,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_PERMISSIONS -> {
                                        SettingPermissionPage(
                                            onEditingBackgroundPermission = {
                                                navigateTo(AppSubPage.SETTINGS_BACKGROUND_RUNNING)
                                            },
                                            onEditingStoragePermission = {
                                                folderPathChooserFolderId = null
                                                navigateTo(AppSubPage.SETTINGS_STORAGE_PERMISSION)
                                            },
                                            onEditingPositionPermission = {
                                                navigateTo(AppSubPage.SETTINGS_POSITION_PERMISSION)
                                            },
                                            showLocalNetworkPermission = localNetworkPermissionRequired,
                                            onEditingLocalNetworkPermission = {
                                                navigateTo(AppSubPage.SETTINGS_LOCAL_NETWORK_PERMISSION)
                                            },
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_LOCAL_NETWORK_PERMISSION -> {
                                        SettingLocalNetworkPermissionPage(
                                            granted = localNetworkAccessGranted,
                                            onRequestPermission = onRequestLocalNetworkAccess,
                                            onOpenSystemSettings = onOpenAppDetailsSettings,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_POSITION_PERMISSION -> {
                                        SettingPositionPermissionPage(
                                            wifiNameAccessGranted = wifiNameAccessGranted,
                                            onRequestWifiNameAccess = onRequestWifiNameAccess,
                                            onOpenLocationSettings = onOpenLocationSettings,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_BACKUP -> {
                                        SettingBackupPage(
                                            uiState = backupUiState,
                                            snackbarHostState = plainPageSnackbarHostState,
                                            onExport = backupViewModel::requestExport,
                                            onImport = backupViewModel::requestImport,
                                            onConfirmImport = backupViewModel::confirmImport,
                                            onCancelImport = backupViewModel::cancelImport,
                                            onMessageShown = backupViewModel::clearMessage,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_COMMON -> {
                                        SettingCommonScreen(
                                            uiState = settingUiState,
                                            settingViewModel = settingViewModel,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_STORAGE -> {
                                        SettingStorageScreen(
                                            uiState = settingUiState,
                                            settingViewModel = settingViewModel,
                                            onEditingStoragePermission = {
                                                folderPathChooserFolderId = null
                                                navigateTo(AppSubPage.SETTINGS_STORAGE_PERMISSION)
                                            },
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_WEBUI -> {
                                        SettingWebuiScreen(
                                            uiState = settingUiState,
                                            settingViewModel = settingViewModel,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_CONNECTION -> {
                                        SettingConnectionScreen(
                                            uiState = settingUiState,
                                            settingViewModel = settingViewModel,
                                            onEditingListenAddresses = {
                                                navigateTo(AppSubPage.SETTINGS_LISTEN_EDIT)
                                            },
                                            onEditingDiscoverServers = {
                                                navigateTo(AppSubPage.SETTINGS_DISCOVERY_EDIT)
                                            },
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.SETTINGS_AUTOSTART -> {
                                        SettingAutoStartScreen(
                                            settingViewModel = settingViewModel,
                                            onNavigateTo = ::navigateTo,
                                            pagePaddingHorizontal = pagePaddingHorizontal,
                                            padding = subPagePadding,
                                        )
                                    }

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_CONNECTION -> DevicesScreen(
                                        uiPadding = subPagePadding,
                                        pagePaddingHorizontal = pagePaddingHorizontal,
                                        uiState = devicesUiState,
                                        coreState = coreUiState.state,
                                        topAppBarScrollBehavior = plainScrollBehavior,
                                        onRefresh = devicesViewModel::refresh,
                                        onAddPendingDevice = { device ->
                                            editingDevice = null
                                            pendingDeviceToAdd = device
                                            navigateTo(AppSubPage.DEVICE_ADD)
                                        },
                                        onDismissPendingDevice = devicesViewModel::dismissPendingDevice,
                                        onIgnorePendingDevice = devicesViewModel::ignorePendingDevice,
                                        onPauseDevice = devicesViewModel::pauseDevice,
                                        onEditDevice = { device ->
                                            editingDevice = device
                                            pendingDeviceToAdd = null
                                            navigateTo(AppSubPage.DEVICE_ADD)
                                        },
                                    )

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_CORE -> CoreScreen(
                                        uiPadding = subPagePadding,
                                        pagePaddingHorizontal = pagePaddingHorizontal,
                                        uiState = coreUiState,
                                        onStartAction = if (coreUiState.isStarted) {
                                            coreViewModel::onStopClicked
                                        } else {
                                            coreViewModel::onStartClicked
                                        },
                                        visiblePages = visibleBottomBarPages,
                                        snackbarHostState = snackbarHostState,
                                        developerModeEnabled = developerModeEnabled,
                                        onModifyDeveloperMode = onModifyDeveloperMode,
                                        onNavigateTo = ::navigateTo,
                                        onSwitchTo = ::requestSwitchToPageMain,
                                    )

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_WEBUI ->  WebviewScreen(
                                        uiPadding = subPagePadding,
                                        coreState = coreUiState.state,
                                        topAppBarScrollBehavior = plainScrollBehavior,
                                        webUiUrl = if (coreUiState.state == CoreState.RUNNING) {
                                            webUiUrlProvider()
                                        } else {
                                            null
                                        },
                                        reloadToken = webUiReloadToken,
                                        webView = webView,
                                    )

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_RECENT_CHANGE -> RecentChangesScreen(
                                        uiPadding = subPagePadding,
                                        pagePaddingHorizontal = pagePaddingHorizontal,
                                        uiState = recentChangesUiState,
                                        coreState = coreUiState.state,
                                        topAppBarScrollBehavior = plainScrollBehavior,
                                        onRefresh = recentChangesViewModel::refresh,
                                    )

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_SETTING -> SettingScreen(
                                        uiPadding = subPagePadding,
                                        pagePaddingHorizontal = pagePaddingHorizontal,
                                        uiState = settingUiState,
                                        developerModeEnabled = developerModeEnabled,
                                        onNavigateTo = ::navigateTo,
                                    )

                                    AppSubPage.MAIN_NONEXIST_NAVIGATOR_FOLDER -> FoldersScreen(
                                        uiPadding = subPagePadding,
                                        pagePaddingHorizontal = pagePaddingHorizontal,
                                        uiState = foldersUiState,
                                        coreState = coreUiState.state,
                                        topAppBarScrollBehavior = plainScrollBehavior,
                                        onRefresh = foldersViewModel::refresh,
                                        onAddPendingFolder = { folder ->
                                            editingFolder = null
                                            pendingFolderToAdd = folder
                                            selectedFolderPath = null
                                            folderPathChooserFolderId = null
                                            devicesViewModel.refresh()
                                            navigateTo(AppSubPage.FOLDER_ADD)
                                        },
                                        onDismissPendingFolder = foldersViewModel::dismissPendingFolder,
                                        onIgnorePendingFolder = foldersViewModel::ignorePendingFolder,
                                        onSetFolderPaused = foldersViewModel::setFolderPaused,
                                        onRescanFolder = foldersViewModel::rescanFolder,
                                        snackbarHostState = snackbarHostState,
                                        onEditFolder = { folder ->
                                            editingFolder = folder
                                            pendingFolderToAdd = null
                                            selectedFolderPath = folder.path
                                            folderPathChooserFolderId = null
                                            devicesViewModel.refresh()
                                            navigateTo(AppSubPage.FOLDER_ADD)
                                        },
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

@Serializable
private sealed interface AppRoute : NavKey {
    @Serializable
    data object Main : AppRoute

    @Serializable
    data class Plain(val page: AppSubPage) : AppRoute
}

internal val AppPage.icon: ImageVector
    get() = when (this) {
        AppPage.DEVICES -> MingCuteIcons.Device
        AppPage.FOLDERS -> MingCuteIcons.Folder
        AppPage.CORE -> MingCuteIcons.Home4
        AppPage.WEBUI -> MingCuteIcons.Earth4Filled
        AppPage.RECENT_CHANGES -> MingCuteIcons.FileDownload
        AppPage.SETTINGS -> MingCuteIcons.Settings
    }

/** Plain page route used when [AppPage] is not part of the visible bottom bar. */
private fun AppPage.plainSubPage(): AppSubPage = when (this) {
    AppPage.DEVICES -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_CONNECTION
    AppPage.FOLDERS -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_FOLDER
    AppPage.CORE -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_CORE
    AppPage.WEBUI -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_WEBUI
    AppPage.RECENT_CHANGES -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_RECENT_CHANGE
    AppPage.SETTINGS -> AppSubPage.MAIN_NONEXIST_NAVIGATOR_SETTING
}

@Serializable
internal enum class AppSubPage {
    DEBUG,
    DEVICE_ADD,
    FOLDER_ADD,
    ABOUT,
    LICENCE,
    SETTINGS_COMMON,
    SETTINGS_STORAGE,
    SETTINGS_WEBUI,
    SETTINGS_CONNECTION,
    SETTINGS_AUTOSTART,
    SETTINGS_LISTEN_EDIT,
    SETTINGS_DISCOVERY_EDIT,
    SETTINGS_STORAGE_PERMISSION,
    SETTINGS_CORE_MANAGE,
    SETTINGS_BACKGROUND_RUNNING,
    SETTINGS_BACKGROUND_RUNNING_NETWORK,
    SETTINGS_BACKGROUND_RUNNING_BATTERY,
    SETTINGS_BACKGROUND_RUNNING_DURATION,
    SETTINGS_BACKGROUND_RUNNING_ADVANCED,
    SETTINGS_POSITION_PERMISSION,
    SETTINGS_LOCAL_NETWORK_PERMISSION,
    SETTINGS_PERMISSIONS,
    SETTINGS_THEME,
    SETTINGS_BACKUP,
    DEV,
    MAIN_NONEXIST_NAVIGATOR_CONNECTION,
    MAIN_NONEXIST_NAVIGATOR_FOLDER,
    MAIN_NONEXIST_NAVIGATOR_CORE,
    MAIN_NONEXIST_NAVIGATOR_WEBUI,
    MAIN_NONEXIST_NAVIGATOR_RECENT_CHANGE,
    MAIN_NONEXIST_NAVIGATOR_SETTING,
}

private val AppSubPage.titleResource: StringResource
    get() = when (this) {
        AppSubPage.DEBUG -> Res.string.setting_page_debug
        AppSubPage.DEVICE_ADD -> Res.string.common_label_device
        AppSubPage.FOLDER_ADD -> Res.string.common_label_folder
        AppSubPage.ABOUT -> Res.string.about_page_about
        AppSubPage.LICENCE -> Res.string.about_page_licenses
        AppSubPage.SETTINGS_COMMON -> Res.string.setting_page_common
        AppSubPage.SETTINGS_STORAGE -> Res.string.setting_page_storage
        AppSubPage.SETTINGS_WEBUI -> Res.string.setting_page_webui
        AppSubPage.SETTINGS_CONNECTION -> Res.string.setting_page_connection
        AppSubPage.SETTINGS_AUTOSTART -> Res.string.setting_page_autostart
        AppSubPage.SETTINGS_LISTEN_EDIT -> Res.string.setting_page_listen_addresses
        AppSubPage.SETTINGS_DISCOVERY_EDIT -> Res.string.setting_page_discovery_servers
        AppSubPage.SETTINGS_STORAGE_PERMISSION -> Res.string.setting_page_storage_permission
        AppSubPage.SETTINGS_CORE_MANAGE -> Res.string.setting_page_core_management
        AppSubPage.SETTINGS_BACKGROUND_RUNNING -> Res.string.setting_page_background_running
        AppSubPage.SETTINGS_BACKGROUND_RUNNING_NETWORK -> Res.string.setting_page_network_conditions
        AppSubPage.SETTINGS_BACKGROUND_RUNNING_BATTERY -> Res.string.setting_page_battery_conditions
        AppSubPage.SETTINGS_BACKGROUND_RUNNING_DURATION -> Res.string.setting_page_time_ranges
        AppSubPage.SETTINGS_BACKGROUND_RUNNING_ADVANCED -> Res.string.common_advanced
        AppSubPage.SETTINGS_POSITION_PERMISSION -> Res.string.setting_page_location_permission
        AppSubPage.SETTINGS_LOCAL_NETWORK_PERMISSION -> Res.string.setting_page_local_network_permission
        AppSubPage.SETTINGS_PERMISSIONS -> Res.string.setting_page_permissions
        AppSubPage.SETTINGS_THEME -> Res.string.setting_page_appearance
        AppSubPage.SETTINGS_BACKUP -> Res.string.setting_page_backup
        AppSubPage.DEV -> Res.string.setting_page_developer_settings
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_CORE -> Res.string.core_page_home
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_WEBUI -> Res.string.webui_page_webui
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_FOLDER -> Res.string.folder_page_folders
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_SETTING -> Res.string.setting_page_settings
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_RECENT_CHANGE -> Res.string.recent_page_recent_changes
        AppSubPage.MAIN_NONEXIST_NAVIGATOR_CONNECTION -> Res.string.device_page_connections
    }

@Composable
internal fun AppSubPage.localizedTitle(): String = stringResource(titleResource)
