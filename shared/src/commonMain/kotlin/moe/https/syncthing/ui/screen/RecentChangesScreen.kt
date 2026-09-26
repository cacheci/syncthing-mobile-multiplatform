package moe.https.syncthing.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.core.SyncthingRecentChange
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.component.CoreNotReadyTakePlace
import moe.https.syncthing.ui.component.ValueRow
import moe.https.syncthing.ui.model.RecentChangesUiState
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.util.toReadable
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder

@Composable
internal fun RecentChangesScreen(
    uiState: RecentChangesUiState,
    coreState: CoreState,
    topAppBarScrollBehavior: ScrollBehavior,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    uiPadding: PaddingValues,
    pagePaddingHorizontal: Dp,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefresh(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        pullToRefreshState = pullToRefreshState,
        topAppBarScrollBehavior = topAppBarScrollBehavior,
        refreshTexts = listOf(stringResource(Res.string.common_pull_to_refresh), stringResource(Res.string.common_release_to_refresh)),
    ) {
        when {
            coreState != CoreState.RUNNING -> CoreNotReadyTakePlace(
                title = stringResource(Res.string.common_core_not_running),
                message = stringResource(Res.string.recent_changes_requires_core),
            )

            uiState.isLoading && uiState.changes.isEmpty() -> {}

            uiState.errorMessage != null -> CoreNotReadyTakePlace(
                title = stringResource(Res.string.common_read_failed),
                message = uiState.errorMessage,
                isError = true,
            ) {
                TextButton(
                    text = stringResource(Res.string.common_action_refresh),
                    onClick = onRefresh,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            uiState.hasLoaded && uiState.changes.isEmpty() -> {
                CoreNotReadyTakePlace(
                    title = stringResource(Res.string.recent_changes_empty_title),
                    message = stringResource(Res.string.recent_changes_empty_message),
                ) {
                    TextButton(
                        text = stringResource(Res.string.common_action_refresh),
                        onClick = onRefresh,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            else -> Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(uiPadding)
                    .padding(horizontal = pagePaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.changes.forEach { change ->
                    key(change.id) {
                        RecentChangeCard(change)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentChangeCard(change: SyncthingRecentChange) {
    val isFolder = change.itemType == "dir" || change.itemType == "folder"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row ( horizontalArrangement = Arrangement.spacedBy(8.dp) ) {
                Icon(
                    contentDescription = "",
                    imageVector = if (isFolder) MiuixIcons.Folder else MiuixIcons.File,
                    tint = AppTheme.colorScheme.onBackground
                )
                Text(
                    text = if (change.action == "deleted") "- " + change.path else "~ " + change.path,
                    color = if (change.action == "deleted") {
                        AppTheme.statusColors.fail
                    } else {
                        AppTheme.colorScheme.primary
                    },
                    style = AppTheme.textStyles.headline1,
                    fontWeight = FontWeight.Medium,
                )
            }
            HorizontalDivider()
            ValueRow(
                label = stringResource(Res.string.common_label_folder),
                value = change.folderLabel?.takeIf(String::isNotBlank) ?: change.folderId,
            )
            change.modifiedBy?.let { modifiedBy ->
                ValueRow(label = stringResource(Res.string.common_label_device), value = if (change.source == SyncthingRecentChange.Source.LOCAL ) stringResource(Res.string.recent_label_this_device) else modifiedBy)
            }
            ValueRow(label = stringResource(Res.string.recent_label_time), value = change.time.toReadable(), valueSingleLine = false)
        }
    }
}
