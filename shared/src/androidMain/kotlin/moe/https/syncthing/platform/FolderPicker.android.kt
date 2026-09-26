package moe.https.syncthing.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import java.io.File

@Composable
actual fun rememberFolderPicker(
    onResult: (FolderPickerResult) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val readFolderFailedMessage = stringResource(Res.string.folder_error_read_selected_folder)
    val localFolderRequiredMessage = stringResource(Res.string.folder_error_local_folder_required)
    val unknownFolderPathMessage = stringResource(Res.string.folder_error_unknown_folder_path)
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri == null) {
            currentOnResult(FolderPickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }

        val result = runCatching {
            persistDirectoryPermission(context, uri)
            FolderPickerResult.Selected(
                resolveDirectoryPath(
                    uri = uri,
                    localFolderRequiredMessage = localFolderRequiredMessage,
                    unknownFolderPathMessage = unknownFolderPathMessage,
                ),
            )
        }.getOrElse { error ->
            FolderPickerResult.Error(
                error.message ?: readFolderFailedMessage,
            )
        }
        currentOnResult(result)
    }

    return remember(launcher) {
        { launcher.launch(null) }
    }
}

private fun persistDirectoryPermission(context: Context, uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    runCatching {
        context.contentResolver.takePersistableUriPermission(uri, flags)
    }
}

private fun resolveDirectoryPath(
    uri: Uri,
    localFolderRequiredMessage: String,
    unknownFolderPathMessage: String,
): String {
    require(uri.authority == EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY) {
        localFolderRequiredMessage
    }

    val documentId = DocumentsContract.getTreeDocumentId(uri)
    if (documentId.startsWith(RAW_PATH_PREFIX)) {
        return documentId.removePrefix(RAW_PATH_PREFIX)
    }

    val parts = documentId.split(':', limit = 2)
    val volume = parts.firstOrNull().orEmpty()
    val relativePath = parts.getOrNull(1).orEmpty()
    val root = when {
        volume.equals(PRIMARY_VOLUME, ignoreCase = true) ->
            Environment.getExternalStorageDirectory()

        volume.equals(HOME_VOLUME, ignoreCase = true) ->
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        volume.isNotBlank() -> File(STORAGE_ROOT, volume)
        else -> error(unknownFolderPathMessage)
    }

    return if (relativePath.isBlank()) {
        root.absolutePath
    } else {
        File(root, relativePath).absolutePath
    }
}

private const val EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY =
    "com.android.externalstorage.documents"
private const val RAW_PATH_PREFIX = "raw:"
private const val PRIMARY_VOLUME = "primary"
private const val HOME_VOLUME = "home"
private const val STORAGE_ROOT = "/storage"
