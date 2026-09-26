package moe.https.syncthing.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberFolderPicker(
    onResult: (FolderPickerResult) -> Unit,
): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    val unavailableMessage = stringResource(Res.string.folder_ios_folder_access_unavailable)
    return remember(unavailableMessage) {
        {
            // TODO: Persist a security-scoped bookmark when iOS folder synchronization is enabled.
            currentOnResult.value(
                FolderPickerResult.Error(unavailableMessage),
            )
        }
    }
}
