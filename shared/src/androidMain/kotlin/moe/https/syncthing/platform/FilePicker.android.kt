package moe.https.syncthing.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import java.io.ByteArrayOutputStream
import java.io.IOException

@Composable
actual fun rememberPemFilePicker(
    onResult: (FilePickerResult) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val fileTooLargeMessage = stringResource(Res.string.setting_error_pem_file_too_large)
    val readFileFailedMessage = stringResource(Res.string.setting_error_read_selected_file)
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            currentOnResult(FilePickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        val result = runCatching {
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    totalBytes += read
                    if (totalBytes > MAX_PEM_FILE_BYTES) {
                        throw IOException(fileTooLargeMessage)
                    }
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            } ?: throw IOException(readFileFailedMessage)
            FilePickerResult.Selected(content)
        }.getOrElse { error ->
            FilePickerResult.Error(error.message ?: readFileFailedMessage)
        }
        currentOnResult(result)
    }

    return remember(launcher) {
        {
            launcher.launch(
                arrayOf(
                    "application/x-pem-file",
                    "application/pkix-cert",
                    "application/octet-stream",
                    "text/plain",
                ),
            )
        }
    }
}

private const val MAX_PEM_FILE_BYTES = 1024 * 1024
