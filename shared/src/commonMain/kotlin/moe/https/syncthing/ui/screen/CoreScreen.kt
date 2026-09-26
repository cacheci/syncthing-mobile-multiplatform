package moe.https.syncthing.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.https.syncthing.AppSubPage
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.core.displayBackgroundColor
import moe.https.syncthing.core.displayColor
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.about_app
import moe.https.syncthing.generated.resources.about_licenses_summary
import moe.https.syncthing.generated.resources.about_page_about
import moe.https.syncthing.generated.resources.about_page_licenses
import moe.https.syncthing.generated.resources.common_error
import moe.https.syncthing.generated.resources.common_not_available
import moe.https.syncthing.generated.resources.common_not_running
import moe.https.syncthing.generated.resources.common_paused
import moe.https.syncthing.generated.resources.common_unnamed
import moe.https.syncthing.generated.resources.core_action_start
import moe.https.syncthing.generated.resources.core_action_stop
import moe.https.syncthing.generated.resources.core_developer_mode_already_enabled
import moe.https.syncthing.generated.resources.core_developer_mode_enabled
import moe.https.syncthing.generated.resources.core_device_name
import moe.https.syncthing.generated.resources.core_download_rate
import moe.https.syncthing.generated.resources.core_memory_usage
import moe.https.syncthing.generated.resources.core_total_file_size
import moe.https.syncthing.generated.resources.core_upload_rate
import moe.https.syncthing.generated.resources.core_uptime
import moe.https.syncthing.ui.component.MessageCard
import moe.https.syncthing.ui.model.AppPage
import moe.https.syncthing.ui.model.CoreUiState
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.theme.Syncthing
import moe.https.syncthing.ui.util.displayName
import moe.https.syncthing.ui.util.formatBitsPerSecond
import moe.https.syncthing.ui.util.formatBytes
import moe.https.syncthing.ui.util.formatDuration
import moe.https.syncthing.ui.util.localizedTitle
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
internal fun CoreScreen(
    uiState: CoreUiState,
    snackbarHostState: SnackbarHostState,
    onStartAction: () -> Unit,
    modifier: Modifier = Modifier,
    uiPadding: PaddingValues,
    pagePaddingHorizontal: Dp,
    developerModeEnabled: Boolean,
    visiblePages: Set<AppPage>,
    onModifyDeveloperMode: () -> Unit,
    onNavigateTo: (AppSubPage) -> Unit,
    onSwitchTo: (AppPage) -> Unit,
) {
    @Composable
    fun InfoComponent(
        title: String,
        summary: String?,
    ) {
        BasicComponent(
            title = title,
            summary = summary ?: "-",
            insideMargin = PaddingValues(vertical = 10.dp, horizontal = 16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(uiPadding)
            .padding(horizontal = pagePaddingHorizontal, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CoreCard(
            uiState = uiState,
            developerModeEnabled = developerModeEnabled,
            onModifyDeveloperMode = onModifyDeveloperMode,
            snackbarHostState = snackbarHostState,
        )

        TextButton(
            text = stringResource(
                if (uiState.state == CoreState.STOPPED) Res.string.core_action_start else Res.string.core_action_stop,
            ),
            onClick = onStartAction,
            enabled = uiState.canAction,
            modifier = Modifier.fillMaxWidth(),
        )

        Card {
            Column {
                AppPage.entries.filterNot(visiblePages::contains).forEach{
                    ArrowPreference(
                        title = it.localizedTitle(),
                        onClick = { onSwitchTo(it) },
                    )
                }
            }
        }

        Card {
            Column {
                InfoComponent(title = stringResource(Res.string.core_device_name), summary = uiState.deviceName ?: stringResource(Res.string.common_unnamed))
                InfoComponent(title = stringResource(Res.string.core_uptime), summary = formatDuration(uiState.uptimeSeconds) ?: stringResource(Res.string.common_not_running))
                InfoComponent(
                    title = stringResource(Res.string.core_download_rate),
                    summary = uiState.downloadBytesPerSecond?.let {
                        uiState.downloadedBytes?.let {
                            "${formatBitsPerSecond(uiState.downloadBytesPerSecond)} (${formatBytes(uiState.downloadedBytes)})"
                        } ?: formatBitsPerSecond(uiState.downloadBytesPerSecond)
                    } ?: stringResource(Res.string.common_paused),
                )
                InfoComponent(
                    title = stringResource(Res.string.core_upload_rate),
                    summary = uiState.uploadBytesPerSecond?.let {
                        uiState.uploadedBytes?.let {
                            "${formatBitsPerSecond(uiState.uploadBytesPerSecond)} (${formatBytes(uiState.uploadedBytes)})"
                        } ?: formatBitsPerSecond(uiState.uploadBytesPerSecond)
                    } ?: stringResource(Res.string.common_paused),
                )
                InfoComponent(title = stringResource(Res.string.core_total_file_size), summary = formatBytes(uiState.totalFileSizeBytes))
                InfoComponent(title = stringResource(Res.string.core_memory_usage), summary = formatBytes(uiState.rssBytes))
            }
        }

        uiState.lastError?.let { message ->
            MessageCard(
                title = stringResource(Res.string.common_error),
                message = message,
                isError = true,
            )
        }

        Card {
            ArrowPreference(
                title = stringResource(Res.string.about_page_about),
                summary = stringResource(Res.string.about_app),
                onClick = { onNavigateTo(AppSubPage.ABOUT) },
            )

            ArrowPreference(
                title = stringResource(Res.string.about_page_licenses),
                summary = stringResource(Res.string.about_licenses_summary),
                onClick = { onNavigateTo(AppSubPage.LICENCE) },
            )
        }
    }
}

@Composable
private fun CoreCard (
    uiState: CoreUiState,
    developerModeEnabled: Boolean,
    onModifyDeveloperMode: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var developerModeClickTimes by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val developerModeEnabledMessage = stringResource(Res.string.core_developer_mode_enabled)
    val developerModeAlreadyEnabledMessage = stringResource(Res.string.core_developer_mode_already_enabled)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardColors(
            color = uiState.state.displayBackgroundColor(),
            contentColor = AppTheme.colorScheme.onBackground,
            borderColor = uiState.state.displayColor(),
        ),
    ) {
        Row (
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = {
                    if (!developerModeEnabled) {
                        developerModeClickTimes += 1
                        if (developerModeClickTimes >= 10) {
                            onModifyDeveloperMode()
                            developerModeClickTimes = 0
                            scope.launch { snackbarHostState.showSnackbar(developerModeEnabledMessage) }
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar(developerModeAlreadyEnabledMessage) }
                    }
                }
            ),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = uiState.state.displayName(),
                    fontWeight = FontWeight.Medium,
                    style = AppTheme.textStyles.title3,
                )
                Text(
                    uiState.version ?: stringResource(Res.string.common_not_available),
                    fontWeight = FontWeight.Medium,
                    style = AppTheme.textStyles.body1,
                    color = uiState.state.displayColor()
                )
            }
            Icon (
                imageVector = Syncthing,
                contentDescription = "",
                modifier = Modifier
                    .size(72.dp)
                    .offset(x = 16.dp, y = 16.dp)
                    .scale(1.6f),
                tint = uiState.state.displayColor(),
            )
        }
    }
}
