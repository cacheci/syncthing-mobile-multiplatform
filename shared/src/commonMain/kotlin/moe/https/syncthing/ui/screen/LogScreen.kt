package moe.https.syncthing.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import moe.https.syncthing.core.CoreLogSource
import moe.https.syncthing.ui.component.BlurredSmallTopAppBar
import moe.https.syncthing.ui.component.barBackdropSource
import moe.https.syncthing.ui.model.LogUiState
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.theme.AppTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LogScreen(
    uiState: LogUiState,
    onSourceSelected: (CoreLogSource) -> Unit,
    navigateBack: () -> Unit,
    barBackdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = "DEBUG*",
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton( onClick = navigateBack ) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = stringResource(Res.string.common_action_back),
                    )
                }
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
                .padding(padding)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val logSources = listOf(
                    CoreLogSource.SYNCTHING,
                    CoreLogSource.LAUNCHER,
                    CoreLogSource.CONTROLLER,
                )
                TabRow(
                    tabs = listOf("Syncthing", stringResource(Res.string.log_launcher), stringResource(Res.string.log_controller)),
                    selectedTabIndex = logSources.indexOf(uiState.source).coerceAtLeast(0),
                    onTabSelected = { index -> onSourceSelected(logSources[index]) },
                )

                Text(
                    text = if (uiState.refreshedAt != null) {
                        stringResource(Res.string.log_label_last_refresh, uiState.refreshedAt)
                    } else {
                        stringResource(Res.string.log_label_not_refreshed)
                    },
                    style = AppTheme.textStyles.footnote2,
                    color = AppTheme.colorScheme.onSurfaceVariantSummary,
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.error != null -> Text(
                                text = uiState.error,
                                color = AppTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(20.dp),
                            )

                            uiState.content.isBlank() -> Text(
                                text = stringResource(Res.string.log_empty_logs),
                                color = AppTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.align(Alignment.Center),
                            )

                            else -> SelectionContainer {
                                Text(
                                    text = uiState.content,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(verticalScrollState)
                                        .horizontalScroll(horizontalScrollState)
                                        .padding(12.dp),
                                    style = AppTheme.textStyles.body2,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
