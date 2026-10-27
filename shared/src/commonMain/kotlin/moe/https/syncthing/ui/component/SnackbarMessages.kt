package moe.https.syncthing.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.https.syncthing.core.PendingMessage
import top.yukonga.miuix.kmp.basic.SnackbarHostState

@Composable
internal fun <T> SnackbarMessages(
    messages: List<PendingMessage<T>>,
    onConsume: (PendingMessage<T>) -> Boolean,
    hostState: SnackbarHostState,
    withDismissAction: Boolean = false,
    text: (T) -> String,
) {
    key(hostState) {
        val scope = rememberCoroutineScope()
        LaunchedEffect(messages) {
            messages.forEach { message ->
                if (onConsume(message)) {
                    // A separate job prevents consuming the event from cancelling its display.
                    scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        val result = async(start = CoroutineStart.UNDISPATCHED) {
                            hostState.showSnackbar(text(message.value), withDismissAction = withDismissAction)
                        }
                        val snackbar = hostState.newestSnackbarData()
                        try {
                            result.await()
                        } finally {
                            // Miuix retains cancelled requests; do not replay them on page re-entry.
                            withContext(NonCancellable) { snackbar?.dismiss() }
                        }
                    }
                }
            }
        }
    }
}
