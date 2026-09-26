package moe.https.syncthing.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberPemFilePicker(
    onResult: (FilePickerResult) -> Unit,
): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    val unavailableMessage = stringResource(Res.string.setting_ios_file_import_unavailable)
    return remember(unavailableMessage) {
        {
            // TODO: Present UIDocumentPickerViewController when iOS core configuration is enabled.
            currentOnResult.value(
                FilePickerResult.Error(unavailableMessage),
            )
        }
    }
}
