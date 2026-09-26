package moe.https.syncthing.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.component.CoreNotReadyTakePlace
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@Composable
internal fun WebviewScreen(
    coreState: CoreState,
    topAppBarScrollBehavior: ScrollBehavior,
    webUiUrl: String?,
    reloadToken: Int,
    webView: @Composable (
        url: String,
        reloadToken: Int,
        onScroll: (deltaY: Float, isAtTop: Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
    modifier: Modifier = Modifier,
    uiPadding: PaddingValues,
) {
    if (coreState == CoreState.RUNNING && webUiUrl != null) {
        LaunchedEffect(webUiUrl, topAppBarScrollBehavior) {
            topAppBarScrollBehavior.state.heightOffset = 0f
            topAppBarScrollBehavior.state.contentOffset = 0f
        }
        val onScroll = remember(topAppBarScrollBehavior) {
            { deltaY: Float, isAtTop: Boolean ->
                val state = topAppBarScrollBehavior.state
                if (isAtTop) {
                    state.heightOffset = 0f
                    state.contentOffset = 0f
                } else {
                    state.heightOffset -= deltaY
                    state.contentOffset -= deltaY
                }
            }
        }
        webView(
            webUiUrl, reloadToken, onScroll,
            modifier
                .fillMaxSize()
                .padding(
                    top = uiPadding.calculateTopPadding(),
                    bottom = uiPadding.calculateBottomPadding() + 8.dp,
                )
        )
        return
    }

    val (title, message) = when (coreState) {
        CoreState.NOT_INSTALLED -> stringResource(Res.string.common_state_core_not_installed) to stringResource(Res.string.webui_core_not_installed_message)
        CoreState.STOPPED -> stringResource(Res.string.common_core_not_running) to stringResource(Res.string.webui_core_stopped_message)
        CoreState.INSTALLING -> stringResource(Res.string.webui_core_installing_title) to stringResource(Res.string.webui_core_installing_message)
        CoreState.STARTING -> stringResource(Res.string.webui_core_starting_title) to stringResource(Res.string.webui_core_starting_message)
        CoreState.STOPPING -> stringResource(Res.string.webui_core_stopping_title) to stringResource(Res.string.webui_unavailable_message)
        CoreState.FAILED -> stringResource(Res.string.common_state_core_failed) to stringResource(Res.string.webui_core_failed_message)
        CoreState.RUNNING -> stringResource(Res.string.webui_address_unavailable) to stringResource(Res.string.webui_address_unavailable_message)
    }

    CoreNotReadyTakePlace(title = title, message = message, modifier = modifier)
}
