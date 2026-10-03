package moe.https.syncthing.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.core.FolderDeviceConfiguration
import moe.https.syncthing.core.NewFolderConfiguration
import moe.https.syncthing.core.RemoteFolderState
import moe.https.syncthing.core.SyncthingDevice
import moe.https.syncthing.core.SyncthingFolder
import moe.https.syncthing.core.SyncthingPendingFolder
import moe.https.syncthing.core.defaultFolderPath
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.common_action_add
import moe.https.syncthing.generated.resources.common_action_block
import moe.https.syncthing.generated.resources.common_action_cancel
import moe.https.syncthing.generated.resources.common_action_confirm
import moe.https.syncthing.generated.resources.common_action_delete
import moe.https.syncthing.generated.resources.common_action_edit
import moe.https.syncthing.generated.resources.common_action_ignore
import moe.https.syncthing.generated.resources.common_action_pause
import moe.https.syncthing.generated.resources.common_action_resume
import moe.https.syncthing.generated.resources.common_action_save
import moe.https.syncthing.generated.resources.common_core_not_running
import moe.https.syncthing.generated.resources.common_label_device
import moe.https.syncthing.generated.resources.common_label_folder
import moe.https.syncthing.generated.resources.common_optional
import moe.https.syncthing.generated.resources.common_paused
import moe.https.syncthing.generated.resources.common_pull_to_refresh
import moe.https.syncthing.generated.resources.common_read_failed
import moe.https.syncthing.generated.resources.common_release_to_refresh
import moe.https.syncthing.generated.resources.common_required
import moe.https.syncthing.generated.resources.common_ungrouped
import moe.https.syncthing.generated.resources.common_unknown
import moe.https.syncthing.generated.resources.folder_action_add_folder
import moe.https.syncthing.generated.resources.folder_action_edit_folder
import moe.https.syncthing.generated.resources.folder_add_ignore_file
import moe.https.syncthing.generated.resources.folder_add_ignore_file_summary
import moe.https.syncthing.generated.resources.folder_block_index
import moe.https.syncthing.generated.resources.folder_block_index_summary
import moe.https.syncthing.generated.resources.folder_cleanup_interval_seconds
import moe.https.syncthing.generated.resources.folder_command
import moe.https.syncthing.generated.resources.folder_delete_folder
import moe.https.syncthing.generated.resources.folder_delete_folder_confirmation
import moe.https.syncthing.generated.resources.folder_delete_local_files
import moe.https.syncthing.generated.resources.folder_edit_ignore_file
import moe.https.syncthing.generated.resources.folder_empty_message
import moe.https.syncthing.generated.resources.folder_empty_title
import moe.https.syncthing.generated.resources.folder_encrypted_type_immutable
import moe.https.syncthing.generated.resources.folder_file_change_detection
import moe.https.syncthing.generated.resources.folder_file_count
import moe.https.syncthing.generated.resources.folder_file_count_size
import moe.https.syncthing.generated.resources.folder_file_pull_order
import moe.https.syncthing.generated.resources.folder_file_versioning
import moe.https.syncthing.generated.resources.folder_forever
import moe.https.syncthing.generated.resources.folder_group
import moe.https.syncthing.generated.resources.folder_id
import moe.https.syncthing.generated.resources.folder_id_summary
import moe.https.syncthing.generated.resources.folder_ignore_comment
import moe.https.syncthing.generated.resources.folder_ignore_editor_instruction
import moe.https.syncthing.generated.resources.folder_ignore_include
import moe.https.syncthing.generated.resources.folder_ignore_patterns
import moe.https.syncthing.generated.resources.folder_ignore_prefix_case_insensitive
import moe.https.syncthing.generated.resources.folder_ignore_prefix_deletable
import moe.https.syncthing.generated.resources.folder_ignore_prefix_negate
import moe.https.syncthing.generated.resources.folder_ignore_wildcard_multiple
import moe.https.syncthing.generated.resources.folder_ignore_wildcard_single
import moe.https.syncthing.generated.resources.folder_local_data
import moe.https.syncthing.generated.resources.folder_location
import moe.https.syncthing.generated.resources.folder_location_hint
import moe.https.syncthing.generated.resources.folder_name
import moe.https.syncthing.generated.resources.folder_new_folder_group
import moe.https.syncthing.generated.resources.folder_no_devices
import moe.https.syncthing.generated.resources.folder_no_password
import moe.https.syncthing.generated.resources.folder_password
import moe.https.syncthing.generated.resources.folder_path
import moe.https.syncthing.generated.resources.folder_pending_sync
import moe.https.syncthing.generated.resources.folder_read_ignore_file_failed
import moe.https.syncthing.generated.resources.folder_receive_encrypted_summary
import moe.https.syncthing.generated.resources.folder_receive_only_summary
import moe.https.syncthing.generated.resources.folder_remote_folder
import moe.https.syncthing.generated.resources.folder_required_unique
import moe.https.syncthing.generated.resources.folder_requires_core
import moe.https.syncthing.generated.resources.folder_rescan_interval_seconds
import moe.https.syncthing.generated.resources.folder_retention_days
import moe.https.syncthing.generated.resources.folder_scan_periodically
import moe.https.syncthing.generated.resources.folder_send_only_summary
import moe.https.syncthing.generated.resources.folder_shared_by
import moe.https.syncthing.generated.resources.folder_status_abnormal
import moe.https.syncthing.generated.resources.folder_status_clean_wait
import moe.https.syncthing.generated.resources.folder_status_cleaning
import moe.https.syncthing.generated.resources.folder_status_error
import moe.https.syncthing.generated.resources.folder_status_needs_sync
import moe.https.syncthing.generated.resources.folder_status_scan_wait
import moe.https.syncthing.generated.resources.folder_status_scanning
import moe.https.syncthing.generated.resources.folder_status_sync_preparing
import moe.https.syncthing.generated.resources.folder_status_sync_wait
import moe.https.syncthing.generated.resources.folder_status_synced
import moe.https.syncthing.generated.resources.folder_status_syncing
import moe.https.syncthing.generated.resources.folder_sync_control
import moe.https.syncthing.generated.resources.folder_sync_direction
import moe.https.syncthing.generated.resources.folder_sync_errors
import moe.https.syncthing.generated.resources.folder_type
import moe.https.syncthing.generated.resources.folder_type_receive_encrypted
import moe.https.syncthing.generated.resources.folder_type_receive_encrypted_full
import moe.https.syncthing.generated.resources.folder_type_receive_only
import moe.https.syncthing.generated.resources.folder_type_send_only
import moe.https.syncthing.generated.resources.folder_type_send_receive
import moe.https.syncthing.generated.resources.folder_unnamed_device
import moe.https.syncthing.generated.resources.folder_versioning
import moe.https.syncthing.generated.resources.folder_versioning_encrypted_unsupported
import moe.https.syncthing.generated.resources.folder_versioning_external_summary
import moe.https.syncthing.generated.resources.folder_versioning_simple_summary
import moe.https.syncthing.generated.resources.folder_versioning_staggered_summary
import moe.https.syncthing.generated.resources.folder_versioning_trashcan_summary
import moe.https.syncthing.generated.resources.folder_versioning_unsupported
import moe.https.syncthing.generated.resources.folder_versions_path
import moe.https.syncthing.generated.resources.folder_versions_to_keep
import moe.https.syncthing.generated.resources.folder_view_full_help
import moe.https.syncthing.generated.resources.folder_warning
import moe.https.syncthing.generated.resources.folder_watch_and_scan
import moe.https.syncthing.ui.component.BlurredSmallTopAppBar
import moe.https.syncthing.ui.component.CheckableInputValueRow
import moe.https.syncthing.ui.component.CheckableValueRow
import moe.https.syncthing.ui.component.CoreNotReadyTakePlace
import moe.https.syncthing.ui.component.GroupedCard
import moe.https.syncthing.ui.component.InfoSwitch
import moe.https.syncthing.ui.component.InfoSwitchCard
import moe.https.syncthing.ui.component.InputValueRow
import moe.https.syncthing.ui.component.MultipleValueRow
import moe.https.syncthing.ui.component.PendingCard
import moe.https.syncthing.ui.component.barBackdropSource
import moe.https.syncthing.ui.model.FoldersUiState
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.util.formatBytes
import moe.https.syncthing.ui.util.localizedDisplayName
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Checkbox
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
import top.yukonga.miuix.kmp.icon.extended.Help
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.scripta.editor.CodeEditor
import top.yukonga.scripta.editor.EditorColors
import top.yukonga.scripta.editor.EditorLanguage
import top.yukonga.scripta.editor.EditorSymbol
import top.yukonga.scripta.editor.rememberSaveableCodeEditorController

@Composable
internal fun FoldersScreen(
    uiState: FoldersUiState,
    coreState: CoreState,
    topAppBarScrollBehavior: ScrollBehavior,
    onRefresh: () -> Unit,
    onAddPendingFolder: (SyncthingPendingFolder) -> Unit,
    onDismissPendingFolder: (SyncthingPendingFolder) -> Unit,
    onIgnorePendingFolder: (SyncthingPendingFolder) -> Unit,
    onEditFolder: (SyncthingFolder) -> Unit,
    onSetFolderPaused: (folderId: String, paused: Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    uiPadding: PaddingValues,
    pagePaddingHorizontal: Dp,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val localizedActionError = uiState.actionError

    LaunchedEffect(uiState.actionError) {
        if (!localizedActionError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(localizedActionError)
        }
    }

    when {
        coreState != CoreState.RUNNING -> CoreNotReadyTakePlace(
            title = stringResource(Res.string.common_core_not_running),
            message = stringResource(Res.string.folder_requires_core),
        )

        uiState.isLoading && uiState.folders.isEmpty() && uiState.pendingFolders.isEmpty() -> {}

        uiState.loadError != null -> CoreNotReadyTakePlace(
            title = stringResource(Res.string.common_read_failed),
            message = uiState.loadError,
            isError = true,
        )

        uiState.hasLoaded && uiState.folders.isEmpty() && uiState.pendingFolders.isEmpty() -> CoreNotReadyTakePlace(
            title = stringResource(Res.string.folder_empty_title),
            message = stringResource(Res.string.folder_empty_message),
        )

        else -> {
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
                    uiState.pendingFolders.forEach { folder ->
                        key("pending:${folder.id}:${folder.source}") {
                            NewFolderCard(
                                folder = folder,
                                enabled = !uiState.isPendingFolderActionInProgress,
                                onAdd = { onAddPendingFolder(folder) },
                                onDismiss = { onDismissPendingFolder(folder) },
                                onIgnore = { onIgnorePendingFolder(folder) },
                            )
                        }
                    }
                    uiState.folders
                        .groupBy { it.group.trim() }
                        .toList()
                        .sortedWith(
                            compareBy<Pair<String, List<SyncthingFolder>>> { it.first.isBlank() }
                                .thenBy { it.first.lowercase() },
                        )
                        .forEach { (group, folders) ->
                            key("group:$group") {
                                GroupedCard (group) {
                                    folders.forEach { folder ->
                                        key(folder.id) {
                                            FolderCard(
                                                folder = folder,
                                                isLoading = !uiState.isLoading,
                                                cornerRadius = CardDefaults.CornerRadius - 6.dp,
                                                onEditFolder = onEditFolder,
                                                onSetPaused = { paused ->
                                                    onSetFolderPaused(folder.id, paused)
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
}

@Composable
private fun FolderCard(
    folder: SyncthingFolder,
    isLoading: Boolean,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    onEditFolder: (SyncthingFolder) -> Unit,
    onSetPaused: (Boolean) -> Unit,
) {
    var holdDown by rememberSaveable { mutableStateOf(false) }
    var foldContentStatus by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = cornerRadius,
        colors = CardDefaults.defaultColors(
            color = AppTheme.colorScheme.secondaryContainer,
            contentColor = AppTheme.colorScheme.onSecondaryContainer,
        ),
        pressFeedbackType = PressFeedbackType.Sink,
        holdDownState = holdDown,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .combinedClickable(
                        onClick = { foldContentStatus = !foldContentStatus },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "●",
                        color = folder.statusColor(),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = folder.label?.takeIf(String::isNotBlank) ?: folder.id,
                        style = AppTheme.textStyles.headline1,
                        color = AppTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = folder.statusName(),
                    color = folder.statusColor(),
                    fontWeight = FontWeight.Medium,
                )
            }

            AnimatedVisibility(
                visible = foldContentStatus,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
            ) {
                Column (verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider()
                    FolderValueRow(stringResource(Res.string.folder_id), folder.id)
                    FolderValueRow(stringResource(Res.string.folder_path), folder.path)
                    FolderValueRow(stringResource(Res.string.folder_type), folder.typeName())
                    FolderValueRow(
                        stringResource(Res.string.folder_local_data),
                        stringResource(Res.string.folder_file_count_size, folder.localFiles, formatBytes(folder.localBytes).orEmpty()),
                    )
                    FolderValueRow(
                        stringResource(Res.string.folder_pending_sync),
                        stringResource(Res.string.folder_file_count_size, folder.needFiles, formatBytes(folder.needBytes).orEmpty()),
                    )
                    if (folder.pullErrors > 0) {
                        FolderValueRow(
                            stringResource(Res.string.folder_sync_errors),
                            stringResource(Res.string.folder_file_count, folder.pullErrors),
                            isError = true,
                        )
                    }
                    Row (
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(if (folder.paused) Res.string.common_action_resume else Res.string.common_action_pause),
                            enabled = isLoading,
                            colors = ButtonDefaults.textButtonColors(
                                color = AppTheme.colorScheme.surfaceContainerHigh,
                                textColor = AppTheme.colorScheme.onSurfaceContainer,
                            ),
                            onClick = { onSetPaused(!folder.paused) },
                        )
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.common_action_edit),
                            enabled = isLoading,
                            colors = ButtonDefaults.textButtonColors(
                                color = AppTheme.colorScheme.surfaceContainerHigh,
                                textColor = AppTheme.colorScheme.onSurfaceContainer,
                            ),
                            onClick = { onEditFolder( folder ) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderValueRow(
    label: String,
    value: String,
    isError: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = AppTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.weight(0.35f),
        )
        Text(
            text = value,
            color = if (isError) AppTheme.colorScheme.error else AppTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.65f),
        )
    }
}

@Composable
private fun NewFolderCard(
    folder: SyncthingPendingFolder,
    enabled: Boolean,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
) {
    PendingCard(title = stringResource(Res.string.folder_remote_folder, folder.name)) {
        Column (
            modifier = Modifier.padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MultipleValueRow(
                label = stringResource(Res.string.folder_id),
                values = listOf(folder.id),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
            MultipleValueRow(
                label = stringResource(Res.string.folder_shared_by),
                values = listOf(folder.sourceName),
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
                    colors = ButtonDefaults.textButtonColors(
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
internal fun AddFolderScreen(
    isSubmitting: Boolean,
    folderGroups: List<String>,
    devices: List<SyncthingDevice>,
    selectedFolderPath: String?,
    onConfirm: (NewFolderConfiguration) -> Unit,
    onRedirectToPathChooserPage: (folderId: String) -> Unit,
    navigateBack: () -> Unit,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
    actionError: String? = null,
    existingFolder: SyncthingFolder? = null,
    pendingFolder: SyncthingPendingFolder? = null,
    onDeleteFolder: (folderId: String, deleteLocalFiles: Boolean) -> Unit = { _, _ -> },
) {
    val isEditingFolder = (existingFolder != null)
    val isAddingRemote = (pendingFolder != null)
    var label by remember(existingFolder, pendingFolder) {
        mutableStateOf(existingFolder?.label ?: pendingFolder?.name.orEmpty())
    }
    var group by remember(existingFolder) { mutableStateOf(existingFolder?.group.orEmpty()) }
    var folderId by remember(existingFolder, pendingFolder) {
        mutableStateOf(existingFolder?.id ?: pendingFolder?.id.orEmpty())
    }
    var versioning by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioning ?: NewFolderConfiguration.Versioning.NONE)
    }
    val versioningOptions = NewFolderConfiguration.Versioning.entries
    var versioningFsPath by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioningFsPath.orEmpty())
    }
    var cleanoutDays by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioningCleanoutDays?.toString().orEmpty())
    }
    var keepVersions by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioningKeep?.toString() ?: "5")
    }
    var cleanupIntervalSeconds by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioningCleanupIntervalSeconds?.toString() ?: "3600")
    }
    var externalCommand by remember(existingFolder) {
        mutableStateOf(existingFolder?.versioningExternalCommand.orEmpty())
    }
    var ignorePatternsEnabled by remember(existingFolder) {
        mutableStateOf(false)
    }
    val initialIgnoreText = existingFolder?.ignorePatterns?.joinToString("\n").orEmpty()
    var acceptedIgnoreText by rememberSaveable(existingFolder?.id) {
        mutableStateOf(initialIgnoreText)
    }
    val ignoreEditorController = rememberSaveableCodeEditorController(
        initialText = initialIgnoreText,
    )
    var fsWatcherEnabled by remember(existingFolder) {
        mutableStateOf(existingFolder?.fsWatcherEnabled ?: true)
    }
    var rescanIntervalSeconds by remember(existingFolder) {
        mutableStateOf(existingFolder?.rescanIntervalSeconds?.toString() ?: "3600")
    }
    var pullOrder by remember(existingFolder) {
        mutableStateOf(existingFolder?.pullOrder ?: NewFolderConfiguration.PullOrder.RANDOM)
    }
    val pullOrderOptions = NewFolderConfiguration.PullOrder.entries
    var blockIndexing by remember(existingFolder) {
        mutableStateOf(existingFolder?.blockIndexing ?: true)
    }
    var folderType by remember(existingFolder) {
        mutableStateOf(
            when (existingFolder?.type) {
                "receiveonly" -> NewFolderConfiguration.Type.RECEIVE_ONLY
                "sendonly" -> NewFolderConfiguration.Type.SEND_ONLY
                "receiveencrypted" -> NewFolderConfiguration.Type.RECEIVE_ENCRYPTED
                else -> NewFolderConfiguration.Type.SEND_RECEIVE
            },
        )
    }
    val folderTypeOptions = if (isEditingFolder) {
        NewFolderConfiguration.Type.entries.filter { type ->
            type != NewFolderConfiguration.Type.RECEIVE_ENCRYPTED ||
                folderType == NewFolderConfiguration.Type.RECEIVE_ENCRYPTED
        }
    } else {
        NewFolderConfiguration.Type.entries
    }
    val folderTypeNames = mapOf(
        NewFolderConfiguration.Type.SEND_RECEIVE to stringResource(Res.string.folder_type_send_receive),
        NewFolderConfiguration.Type.RECEIVE_ONLY to stringResource(Res.string.folder_type_receive_only),
        NewFolderConfiguration.Type.SEND_ONLY to stringResource(Res.string.folder_type_send_only),
        NewFolderConfiguration.Type.RECEIVE_ENCRYPTED to stringResource(Res.string.folder_type_receive_encrypted),
    )
    val isReceiveEncrypted = folderType == NewFolderConfiguration.Type.RECEIVE_ENCRYPTED
    val remoteDevices = devices.filterNot { it.isLocal }
    val remoteDeviceIds = remoteDevices.map { it.id }
    var selectedDeviceIds by remember(existingFolder, pendingFolder, remoteDeviceIds) {
        mutableStateOf(
            existingFolder?.devices?.map { it.deviceId }?.toSet()
                ?: pendingFolder?.let { setOf(it.source) }
                ?: remoteDeviceIds.toSet(),
        )
    }
    var devicePasswords by remember(existingFolder, remoteDeviceIds) {
        mutableStateOf(
            existingFolder?.devices
                ?.associate { it.deviceId to it.encryptionPassword }
                .orEmpty(),
        )
    }
    val defaultPath = if (folderId.isBlank()) null else {
        defaultFolderPath(folderId.trim())
    }
    val canSubmit = (
            (folderId.trim().isNotBlank()) &&
            (listOf(
                cleanoutDays,
                keepVersions,
                cleanupIntervalSeconds,
                rescanIntervalSeconds,
            ).all { value -> (value.toIntWithDefaultForEmpty(0))?.let{ it >= 0 } == true }) &&
            (cleanupIntervalSeconds.toIntWithDefaultForEmpty(3600)?.let{ it <= 31_536_000 } == true) &&
            (versioning != NewFolderConfiguration.Versioning.EXTERNAL || externalCommand.trim().isNotBlank()) &&
            (isReceiveEncrypted || remoteDevices
                .filter { it.untrusted && it.id in selectedDeviceIds }
                .all { device -> devicePasswords[device.id].orEmpty().isNotBlank() }) &&
            (!isSubmitting)
    )

    var showEditorBottomSheet by remember { mutableStateOf(false) }
    var showStIgnoreHelp by remember { mutableStateOf(false) }
    var showDeleteOverlay by rememberSaveable { mutableStateOf(false) }
    var deleteLocalFiles by rememberSaveable { mutableStateOf(false) }
    var showFolderGroupChooseSheet by rememberSaveable { mutableStateOf(false) }
    var chosenGroup by rememberSaveable(existingFolder?.id) { mutableStateOf(group.trim()) }
    val availableFolderGroups = remember(folderGroups, group) {
        (folderGroups + group)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sortedBy { it.lowercase() }
    }
    var newGroup by remember { mutableStateOf("") }

    val uriHandler = LocalUriHandler.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    LaunchedEffect(actionError) {
        if (!actionError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(actionError)
        }
    }

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = stringResource(
                if (isEditingFolder) Res.string.folder_action_edit_folder else Res.string.folder_action_add_folder,
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
                IconButton(
                    enabled = canSubmit,
                    onClick = {
                        onConfirm(
                            NewFolderConfiguration(
                                folderId = folderId,
                                label = label,
                                group = group,
                                path = selectedFolderPath ?: defaultFolderPath(folderId.trim()),
                                versioning = versioning,
                                updateVersioning = existingFolder?.versioningSupported != false,
                                versioningFsPath = versioningFsPath.trim(),
                                versioningCleanoutDays = cleanoutDays.toIntOrNull() ?: 0,
                                versioningKeep = keepVersions.toIntOrNull() ?: 5,
                                versioningCleanupIntervalSeconds = cleanupIntervalSeconds.toIntOrNull() ?: 3600,
                                versioningExternalCommand = externalCommand.trim(),
                                ignorePatterns = if (isEditingFolder) {
                                    acceptedIgnoreText.toIgnorePatternLines()
                                } else {
                                    emptyList()
                                },
                                updateIgnorePatterns = if (isEditingFolder) {
                                    acceptedIgnoreText != initialIgnoreText
                                } else {
                                    ignorePatternsEnabled
                                },
                                fsWatcherEnabled = fsWatcherEnabled,
                                rescanIntervalSeconds = rescanIntervalSeconds.toIntOrNull() ?: 3600,
                                pullOrder = pullOrder,
                                blockIndexing = blockIndexing,
                                type = folderType,
                                devices = remoteDevices
                                    .filter { it.id in selectedDeviceIds }
                                    .map { device ->
                                        FolderDeviceConfiguration(
                                            deviceId = device.id,
                                            encryptionPassword = if (isReceiveEncrypted) {
                                                ""
                                            } else {
                                                devicePasswords[device.id].orEmpty()
                                            },
                                        )
                                    },
                                availableDeviceIds = remoteDeviceIds.toSet(),
                            ),
                        )
                    },
                    content = {
                        Icon(
                            contentDescription = stringResource(
                                if (isEditingFolder) Res.string.common_action_save else Res.string.common_action_add,
                            ),
                            imageVector = MiuixIcons.Ok,
                            tint = if (canSubmit) {
                                AppTheme.colorScheme.onSurface
                            } else AppTheme.colorScheme.disabledOnSurface
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
                .nestedScroll(
                    scrollBehavior.nestedScrollConnection,
                )
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = pagePaddingHorizontal),
            ) {
                InfoSwitchCard(
                    title = stringResource(Res.string.common_label_folder),
                    content = {
                        InputValueRow(
                            label = stringResource(Res.string.folder_id),
                            summary = stringResource(Res.string.folder_id_summary),
                            value = folderId,
                            valueLabel = stringResource(Res.string.folder_required_unique),
                            allowEdit = !isSubmitting && !isEditingFolder && !isAddingRemote,
                            onValueChange = { folderId = it },
                        )

                        InputValueRow(
                            label = stringResource(Res.string.folder_name),
                            value = label,
                            valueLabel = stringResource(Res.string.common_optional),
                            allowEdit = !isSubmitting,
                            onValueChange = { label = it },
                        )

                        ArrowPreference(
                            title = stringResource(Res.string.folder_group),
                            enabled = !isSubmitting,
                            endActions = {
                                Text(
                                    text = if (group.isBlank()) stringResource(Res.string.common_ungrouped) else group.trim(),
                                    fontSize = AppTheme.textStyles.body2.fontSize,
                                    color = if (!isSubmitting) {
                                        AppTheme.colorScheme.onSurfaceVariantSummary
                                    } else {
                                        AppTheme.colorScheme.disabledOnSecondaryVariant
                                    },
                                )
                            },
                            onClick = {
                                chosenGroup = group.trim()
                                showFolderGroupChooseSheet = true
                            },
                        )

                        ArrowPreference(
                            title = stringResource(Res.string.folder_location),
                            summary = selectedFolderPath ?: defaultPath ?: stringResource(Res.string.folder_location_hint),
                            onClick = {
                                onRedirectToPathChooserPage(folderId.trim())
                            },
                            enabled = !isSubmitting,
                        )
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.common_label_device),
                    content = {
                        if (remoteDevices.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.folder_no_devices),
                                color = AppTheme.colorScheme.disabledOnSecondaryVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
                            )
                        } else {
                            remoteDevices.forEach { device ->
                                AddFolderDevices(
                                    device = device,
                                    isSubmitting = isSubmitting,
                                    selected = device.id in selectedDeviceIds,
                                    remoteFolderState = existingFolder?.devices
                                        ?.firstOrNull { it.deviceId == device.id }
                                        ?.remoteFolderState,
                                    encryptionPassword = if (isReceiveEncrypted) null else {
                                        devicePasswords[device.id].orEmpty()
                                    },
                                    onSelectedChange = { selected ->
                                        selectedDeviceIds = if (selected) {
                                            selectedDeviceIds + device.id
                                        } else {
                                            selectedDeviceIds - device.id
                                        }
                                    },
                                    onEncryptionPasswordChange = { password ->
                                        devicePasswords = devicePasswords + (device.id to password)
                                    },
                                )
                            }
                        }
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.folder_versioning),
                    content = {
                        WindowDropdownPreference(
                            title = stringResource(Res.string.folder_file_versioning),
                            summary = when {
                                isReceiveEncrypted -> stringResource(Res.string.folder_versioning_encrypted_unsupported)
                                versioning == NewFolderConfiguration.Versioning.TRASHCAN ->
                                    stringResource(Res.string.folder_versioning_trashcan_summary)
                                versioning == NewFolderConfiguration.Versioning.SIMPLE ->
                                    stringResource(Res.string.folder_versioning_simple_summary)
                                versioning == NewFolderConfiguration.Versioning.STAGGERED ->
                                    stringResource(Res.string.folder_versioning_staggered_summary)
                                versioning == NewFolderConfiguration.Versioning.EXTERNAL ->
                                    stringResource(Res.string.folder_versioning_external_summary)
                                existingFolder?.versioningSupported == false ->
                                    stringResource(Res.string.folder_versioning_unsupported)
                                else -> null
                            },
                            items = versioningOptions.map { it.localizedDisplayName() },
                            selectedIndex = versioningOptions.indexOf(versioning),
                            enabled = !isSubmitting &&
                                !isReceiveEncrypted &&
                                existingFolder?.versioningSupported != false,
                            onSelectedIndexChange = { selectedIndex ->
                                versioning = versioningOptions[selectedIndex]
                            },
                        )

                        AnimatedVisibility(
                            visible = (versioning != NewFolderConfiguration.Versioning.NONE) && (versioning != NewFolderConfiguration.Versioning.EXTERNAL),
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                        ) {
                            Column {
                                InputValueRow(
                                    label = stringResource(Res.string.folder_retention_days),
                                    value = cleanoutDays,
                                    valueLabel = stringResource(Res.string.folder_forever),
                                    allowEdit = !isSubmitting,
                                    onValueChange = { cleanoutDays = it },
                                    valueValidator = {
                                        cleanoutDays.toIntWithDefaultForEmpty(0)?.let { it >= 0 } == true
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )

                                InputValueRow(
                                    label = stringResource(Res.string.folder_versions_path),
                                    value = versioningFsPath,
                                    valueLabel = ".stversions",
                                    allowEdit = !isSubmitting,
                                    onValueChange = { versioningFsPath = it },
                                )

                                // 简易版本控制
                                AnimatedVisibility(
                                    visible = versioning == NewFolderConfiguration.Versioning.SIMPLE,
                                    enter = expandVertically(
                                        animationSpec = tween(durationMillis = 300)
                                    ),
                                    exit = shrinkVertically(
                                        animationSpec = tween(durationMillis = 300)
                                    ),
                                ) {
                                    Column {
                                        InputValueRow(
                                            label = stringResource(Res.string.folder_versions_to_keep),
                                            value = keepVersions,
                                            valueLabel = "5",
                                            allowEdit = !isSubmitting,
                                            onValueChange = { keepVersions = it },
                                            valueValidator = {
                                                keepVersions.toIntWithDefaultForEmpty(5)
                                                    ?.let{ it > 0 } == true
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        )
                                    }
                                }

                                InputValueRow(
                                    label = stringResource(Res.string.folder_cleanup_interval_seconds),
                                    value = cleanupIntervalSeconds,
                                    valueLabel = "3600",
                                    allowEdit = !isSubmitting,
                                    onValueChange = { cleanupIntervalSeconds = it },
                                    valueValidator = {
                                        cleanupIntervalSeconds.toIntWithDefaultForEmpty(3600)
                                            ?.let { it in 0..31_536_000 } == true
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = versioning == NewFolderConfiguration.Versioning.EXTERNAL,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                        ) {
                            InputValueRow(
                                label = stringResource(Res.string.folder_command),
                                value = externalCommand,
                                valueLabel = stringResource(Res.string.common_required),
                                allowEdit = !isSubmitting,
                                onValueChange = { externalCommand = it },
                                valueValidator = { externalCommand.trim().isNotBlank() },
                            )
                        }
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.folder_ignore_patterns),
                    content = {
                        if (isEditingFolder) {
                            ArrowPreference(
                                title = stringResource(Res.string.folder_edit_ignore_file),
                                enabled = !isReceiveEncrypted,
                                onClick = {
                                    ignoreEditorController.setDocument(acceptedIgnoreText)
                                    showEditorBottomSheet = true
                                }
                            )
                        } else {
                            InfoSwitch(
                                title = stringResource(Res.string.folder_add_ignore_file),
                                summary = stringResource(Res.string.folder_add_ignore_file_summary),
                                checked = ignorePatternsEnabled,
                                enabled = !isSubmitting && !isReceiveEncrypted,
                                onCheckedChange = { ignorePatternsEnabled = it },
                            )
                        }

                        existingFolder?.ignoreError?.let { error ->
                            BasicComponent(
                                title = stringResource(Res.string.folder_read_ignore_file_failed),
                                summary = error,
                                endActions = {
                                    Icon(
                                        imageVector = MiuixIcons.Close,
                                        contentDescription = stringResource(Res.string.folder_warning),
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(AppTheme.colorScheme.error)
                                            .padding(4.dp),
                                        tint = AppTheme.colorScheme.background
                                    )
                                }
                            )
                        }
                    }
                )

                InfoSwitchCard(
                    title = stringResource(Res.string.folder_sync_control),
                    content = {
                        WindowDropdownPreference(
                            title = stringResource(Res.string.folder_file_change_detection),
                            items = listOf(stringResource(Res.string.folder_watch_and_scan), stringResource(Res.string.folder_scan_periodically)),
                            selectedIndex = if (fsWatcherEnabled) 0 else 1,
                            enabled = !isSubmitting,
                            onSelectedIndexChange = { selectedIndex ->
                                fsWatcherEnabled = selectedIndex == 0
                            },
                        )

                        InputValueRow(
                            label = stringResource(Res.string.folder_rescan_interval_seconds),
                            value = rescanIntervalSeconds,
                            valueLabel = "3600",
                            allowEdit = !isSubmitting,
                            onValueChange = { rescanIntervalSeconds = it },
                            valueValidator = { rescanIntervalSeconds.toIntWithDefaultForEmpty(3600)?.let{ it >= 0 } == true },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        WindowDropdownPreference(
                            title = stringResource(Res.string.folder_sync_direction),
                            summary = when {
                                isEditingFolder && isReceiveEncrypted ->
                                    stringResource(Res.string.folder_encrypted_type_immutable)
                                folderType == NewFolderConfiguration.Type.SEND_ONLY ->
                                    stringResource(Res.string.folder_send_only_summary)
                                folderType == NewFolderConfiguration.Type.RECEIVE_ONLY ->
                                    stringResource(Res.string.folder_receive_only_summary)
                                folderType == NewFolderConfiguration.Type.RECEIVE_ENCRYPTED ->
                                    stringResource(Res.string.folder_receive_encrypted_summary)
                                else -> null
                            },
                            items = folderTypeOptions.map { type -> folderTypeNames.getValue(type) },
                            selectedIndex = folderTypeOptions.indexOf(folderType),
                            enabled = !isSubmitting && !(isEditingFolder && isReceiveEncrypted),
                            onSelectedIndexChange = { selectedIndex ->
                                folderType = folderTypeOptions[selectedIndex]
                                if (folderType == NewFolderConfiguration.Type.RECEIVE_ENCRYPTED) {
                                    fsWatcherEnabled = false
                                    versioning = NewFolderConfiguration.Versioning.NONE
                                    ignorePatternsEnabled = false
                                }
                            },
                        )

                        AnimatedVisibility(
                            visible = folderType != NewFolderConfiguration.Type.SEND_ONLY,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = 300)
                            ),
                        ) {
                            WindowDropdownPreference(
                                title = stringResource(Res.string.folder_file_pull_order),
                                items = pullOrderOptions.map { it.localizedDisplayName() },
                                selectedIndex = pullOrderOptions.indexOf(pullOrder),
                                enabled = !isSubmitting,
                                onSelectedIndexChange = { selectedIndex ->
                                    pullOrder = pullOrderOptions[selectedIndex]
                                },
                            )
                        }

                        InfoSwitch(
                            title = stringResource(Res.string.folder_block_index),
                            summary = stringResource(Res.string.folder_block_index_summary),
                            checked = blockIndexing,
                            enabled = !isSubmitting,
                            onCheckedChange = { blockIndexing = it },
                        )
                    }
                )

                if (isEditingFolder) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                        text = stringResource(Res.string.common_action_delete),
                        enabled = !isSubmitting,
                        onClick = {
                            deleteLocalFiles = false
                            showDeleteOverlay = true
                        },
                        colors = ButtonDefaults.textButtonColors(
                            textColor = AppTheme.colorScheme.error,
                            borderColor = AppTheme.colorScheme.error,
                        )
                    )
                }
            }

            WindowDialog(
                title = stringResource(Res.string.folder_group),
                show = showFolderGroupChooseSheet,
                onDismissRequest = { showFolderGroupChooseSheet = false },
                onDismissFinished = { showFolderGroupChooseSheet = false },
            ) {
                Column(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                    Card(
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
                        availableFolderGroups.forEach { folderGroup ->
                            key(folderGroup) {
                                CheckableValueRow(
                                    value = folderGroup,
                                    state = chosenGroup == folderGroup,
                                    dividerColor = AppTheme.colorScheme.onSurfaceContainerHigh,
                                    onStateChange = { chosenGroup = folderGroup },
                                    checkBoxColorSet = CheckboxDefaults.checkboxColors(
                                        uncheckedBackgroundColor = AppTheme.colorScheme.disabledOnSurface
                                    ),
                                )
                            }
                        }
                        CheckableInputValueRow(
                            state = chosenGroup == newGroup && chosenGroup.isNotBlank(),
                            value = newGroup,
                            valueLabel = stringResource(Res.string.folder_new_folder_group),
                            onValueChange = {
                                if (chosenGroup == newGroup && chosenGroup.isNotBlank()) {
                                    chosenGroup = it
                                }
                                newGroup = it
                            },
                            valueValidator = {
                                it.isNotEmpty() && it !in availableFolderGroups
                            },
                            onStateChange = { chosenGroup = newGroup },
                            checkBoxColorSet = CheckboxDefaults.checkboxColors(
                                uncheckedBackgroundColor = AppTheme.colorScheme.disabledOnSurface
                            ),
                            showDivider = false,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextButton(
                            text = stringResource(Res.string.common_action_cancel),
                            modifier = Modifier.weight(1f),
                            onClick = { showFolderGroupChooseSheet = false },
                        )
                        TextButton(
                            text = stringResource(Res.string.common_action_confirm),
                            modifier = Modifier.weight(1f),
                            colors = textButtonColorsPrimary(),
                            onClick = {
                                group = chosenGroup
                                showFolderGroupChooseSheet = false
                            },
                        )
                    }
                }
            }

            WindowBottomSheet(
                title = stringResource(Res.string.folder_edit_ignore_file),
                show = showEditorBottomSheet,
                allowDismiss = true,
                enableNestedScroll = false,
                insideMargin = DpSize.Zero,
                onDismissRequest = { showEditorBottomSheet = false },
                onDismissFinished = { showEditorBottomSheet = false },
                startAction = {
                    IconButton(
                        modifier = Modifier.padding(start = pagePaddingHorizontal),
                        onClick = {
                            ignoreEditorController.setDocument(acceptedIgnoreText)
                            showEditorBottomSheet = false
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = stringResource(Res.string.common_action_cancel),
                            tint = AppTheme.colorScheme.onBackground,
                        )
                    }
                },
                endAction = {
                    IconButton(
                        modifier = Modifier.padding(end = pagePaddingHorizontal),
                        onClick = {
                            acceptedIgnoreText = ignoreEditorController.getText()
                            showEditorBottomSheet = false
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = stringResource(Res.string.common_action_confirm),
                            tint = AppTheme.colorScheme.onBackground,
                        )
                    }
                },
            ) {
                Column ( verticalArrangement = Arrangement.spacedBy(12.dp) ) {
                    Row (
                        modifier = Modifier.padding(horizontal = pagePaddingHorizontal).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(Res.string.folder_ignore_editor_instruction))

                        if ( !showStIgnoreHelp ) Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { showStIgnoreHelp = true },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Help,
                                contentDescription = stringResource(Res.string.common_action_confirm),
                                tint = AppTheme.colorScheme.disabledOnSecondaryVariant,
                            )
                        }
                    }

                    if (showStIgnoreHelp) {
                        StIgnoreHelpItem("(?d)", stringResource(Res.string.folder_ignore_prefix_deletable), pagePaddingHorizontal)
                        StIgnoreHelpItem("(?i)", stringResource(Res.string.folder_ignore_prefix_case_insensitive), pagePaddingHorizontal)
                        StIgnoreHelpItem(" !  ", stringResource(Res.string.folder_ignore_prefix_negate), pagePaddingHorizontal)
                        StIgnoreHelpItem(" *  ", stringResource(Res.string.folder_ignore_wildcard_single), pagePaddingHorizontal)
                        StIgnoreHelpItem(" ** ", stringResource(Res.string.folder_ignore_wildcard_multiple), pagePaddingHorizontal)
                        StIgnoreHelpItem(" // ", stringResource(Res.string.folder_ignore_comment), pagePaddingHorizontal)
                        StIgnoreHelpItem("#include", stringResource(Res.string.folder_ignore_include), pagePaddingHorizontal)
                        Card (
                            modifier = Modifier.padding(horizontal = pagePaddingHorizontal),
                            colors = CardColors(
                                color = AppTheme.colorScheme.surfaceContainerHigh,
                                contentColor = AppTheme.colorScheme.onSurfaceContainer,
                                borderColor = AppTheme.colorScheme.outline,
                            ),
                        ) {
                            ArrowPreference(
                                title = stringResource(Res.string.folder_view_full_help),
                                onClick = { uriHandler.openUri("https://docs.syncthing.net/users/ignoring") }
                            )
                        }
                        TextButton(
                            text = stringResource(Res.string.common_action_confirm),
                            modifier = Modifier
                                .padding(horizontal = pagePaddingHorizontal)
                                .padding(bottom = padding.calculateBottomPadding())
                                .fillMaxWidth(),
                            onClick = { showStIgnoreHelp = false }
                        )
                    } else {
                        Column {
                            Text(
                                text = ".stignore",
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppTheme.colorScheme.secondaryContainer)
                                    .padding(4.dp)
                            )
                            CodeEditor(
                                controller = ignoreEditorController,
                                language = EditorLanguage.PlainText,
                                colors = if (isSystemInDarkTheme()) EditorColors.Default else EditorColors.Light,
                                symbols = listOf(
                                    EditorSymbol(label = "*"),
                                    EditorSymbol(label = "**"),
                                    EditorSymbol(label = "!"),
                                    EditorSymbol(label = "//"),
                                    EditorSymbol(label = "(?d)"),
                                    EditorSymbol(label = "(?i)"),
                                    EditorSymbol(label = "#include", value = "#include "),
                                ),
                                windowInsetsEnabled = true,
                                readOnly = isSubmitting,
                                softWrap = true,
                                overscrollEnabled = false,
                                autoClosePairs = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                            )
                        }
                    }
                }
            }

            OverlayDialog(
                show = showDeleteOverlay,
                title = stringResource(Res.string.folder_delete_folder),
                onDismissRequest = { showDeleteOverlay = false },
                onDismissFinished = {
                    showDeleteOverlay = false
                    deleteLocalFiles = false
                },
            ) {
                Column (
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            Res.string.folder_delete_folder_confirmation,
                            folderId.toCharArray().joinToString("\u200B"),
                        ),
                        color = AppTheme.colorScheme.onSurfaceVariantSummary,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = !isSubmitting,
                                role = Role.Checkbox,
                                onClick = { deleteLocalFiles = !deleteLocalFiles },
                            ),
                    ) {
                        Checkbox(
                            state = ToggleableState(deleteLocalFiles),
                            enabled = !isSubmitting,
                            onClick = { deleteLocalFiles = !deleteLocalFiles },
                        )
                        Text(
                            stringResource(Res.string.folder_delete_local_files),
                            style = AppTheme.textStyles.body2,
                            color = AppTheme.colorScheme.onBackgroundVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(end = 34.dp).fillMaxWidth(),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.common_action_cancel),
                            enabled = !isSubmitting,
                            onClick = { showDeleteOverlay = false },
                        )
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.common_action_delete),
                            enabled = !isSubmitting,
                            onClick = {
                                val shouldDeleteLocalFiles = deleteLocalFiles
                                showDeleteOverlay = false
                                deleteLocalFiles = false
                                onDeleteFolder(folderId, shouldDeleteLocalFiles)
                            },
                            colors = ButtonDefaults.textButtonColors(
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

@Composable
private fun StIgnoreHelpItem(
    item: String,
    text: String,
    pagePaddingHorizontal: Dp,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = pagePaddingHorizontal)
    ) {
        Text(
            text = item,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colorScheme.surface)
                .padding(4.dp)
        )
        Text(text)
    }
}

private fun String.toIgnorePatternLines(): List<String> {
    val normalized = replace("\r\n", "\n").replace('\r', '\n')
    return if (normalized.isEmpty()) emptyList() else normalized.split('\n')
}

@Composable
private fun AddFolderDevices(
    device: SyncthingDevice,
    isSubmitting: Boolean,
    selected: Boolean,
    remoteFolderState: RemoteFolderState?,
    encryptionPassword: String?,
    onSelectedChange: (Boolean) -> Unit,
    onEncryptionPasswordChange: (String) -> Unit,
) {
    Column {
        InfoSwitch (
            title = device.name?.takeIf(String::isNotBlank) ?: stringResource(Res.string.folder_unnamed_device),
            summary = if (device.id == device.name) null else device.id,
            checked = selected,
            enabled = !isSubmitting,
            statusColor = when (remoteFolderState) {
                RemoteFolderState.VALID -> AppTheme.statusColors.ok
                RemoteFolderState.NOT_SHARING -> AppTheme.statusColors.pending
                RemoteFolderState.PAUSED -> AppTheme.statusColors.disconnected
                RemoteFolderState.UNKNOWN -> AppTheme.statusColors.fail
                null -> AppTheme.statusColors.down
            },
            onCheckedChange = onSelectedChange,
        )
        encryptionPassword?.let {
            AnimatedVisibility(
                visible = selected,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
            ) {
                InputValueRow(
                    label = stringResource(Res.string.folder_password),
                    value = it,
                    onValueChange = onEncryptionPasswordChange,
                    valueValidator = {
                        !(device.untrusted && encryptionPassword.isBlank())
                    },
                    valueLabel = stringResource(Res.string.folder_no_password),
                    allowEdit = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        }
    }
}

@Composable
private fun SyncthingFolder.statusName(): String = when {
    paused -> stringResource(Res.string.common_paused)
    pullErrors > 0 -> stringResource(Res.string.folder_status_error)
    state == "idle" && needFiles == 0L -> stringResource(Res.string.folder_status_synced)
    state == "scanning" -> stringResource(Res.string.folder_status_scanning)
    state == "scan-wait" -> stringResource(Res.string.folder_status_scan_wait)
    state == "sync-wait" -> stringResource(Res.string.folder_status_sync_wait)
    state == "sync-preparing" -> stringResource(Res.string.folder_status_sync_preparing)
    state == "syncing" -> stringResource(Res.string.folder_status_syncing)
    state == "clean-wait" -> stringResource(Res.string.folder_status_clean_wait)
    state == "cleaning" -> stringResource(Res.string.folder_status_cleaning)
    state == "error" -> stringResource(Res.string.folder_status_abnormal)
    needFiles > 0 -> stringResource(Res.string.folder_status_needs_sync)
    else -> state.ifBlank { stringResource(Res.string.common_unknown) }
}

@Composable
private fun SyncthingFolder.statusColor(): Color = when {
    paused -> AppTheme.statusColors.down
    pullErrors > 0 || state == "error" -> AppTheme.statusColors.fail
    state == "idle" && needFiles == 0L -> AppTheme.statusColors.ok
    else -> AppTheme.statusColors.pending
}

@Composable
private fun SyncthingFolder.typeName(): String = when (type) {
    "sendreceive" -> stringResource(Res.string.folder_type_send_receive)
    "sendonly" -> stringResource(Res.string.folder_type_send_only)
    "receiveonly" -> stringResource(Res.string.folder_type_receive_only)
    "receiveencrypted" -> stringResource(Res.string.folder_type_receive_encrypted_full)
    else -> type.ifBlank { stringResource(Res.string.common_unknown) }
}

private fun String.toIntWithDefaultForEmpty( default: Int ): Int? {
    return if ( isBlank() ) default else toIntOrNull()
}
