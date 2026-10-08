package moe.https.syncthing.core

import kotlinx.serialization.Serializable
import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.getString

@Serializable
data class BackupManifest(
    val formatVersion: Int,
    val createdAt: String,
    val app: AppInfo,
    val core: CoreInfo,
) {
    @Serializable
    data class AppInfo(
        val applicationId: String,
        val versionCode: Long,
        val versionName: String,
    )

    @Serializable
    data class CoreInfo(
        val id: String,
        val source: String?,
        val version: String?,
        val commit: String?,
    )

    fun validateForImport(
        supportedFormatVersion: Int,
        currentApplicationId: String,
        currentVersionCode: Long,
    ) {
        if (formatVersion != supportedFormatVersion) {
            throw BackupManifestValidationException(BackupManifestValidationError.UNSUPPORTED_FORMAT, formatVersion.toString())
        }
        if (app.applicationId != currentApplicationId) {
            throw BackupManifestValidationException(BackupManifestValidationError.OTHER_APPLICATION, app.applicationId)
        }
        if (app.versionCode < 0L) {
            throw BackupManifestValidationException(BackupManifestValidationError.MISSING_APP_VERSION)
        }
        if (app.versionCode > currentVersionCode) {
            throw BackupManifestValidationException(BackupManifestValidationError.NEWER_APP_VERSION)
        }
        if (core.id.isBlank() || core.version.isNullOrBlank()) {
            throw BackupManifestValidationException(BackupManifestValidationError.MISSING_CORE_VERSION)
        }
    }
}

// The platform may wrap this exception; its reason is retained for localized UI messages.
enum class BackupManifestValidationError(val diagnosticMessage: String) {
    UNSUPPORTED_FORMAT("Unsupported backup format version"),
    OTHER_APPLICATION("Backup belongs to another application"),
    MISSING_APP_VERSION("Backup manifest is missing the app version ID"),
    NEWER_APP_VERSION("Backup was created by a newer app version; upgrade the app first"),
    MISSING_CORE_VERSION("Backup manifest is missing core version information"),
}

class BackupManifestValidationException(
    val reason: BackupManifestValidationError,
    val detail: String? = null,
) : IllegalArgumentException(
    if (detail.isNullOrBlank()) reason.diagnosticMessage else "${reason.diagnosticMessage}: $detail",
), UserFacingError {
    override suspend fun localizedUserMessage(): String = when (reason) {
        BackupManifestValidationError.UNSUPPORTED_FORMAT ->
            getString(Res.string.setting_backup_error_format, detail.orEmpty())
        BackupManifestValidationError.OTHER_APPLICATION ->
            getString(
                Res.string.setting_backup_error_application,
                detail?.takeIf(String::isNotBlank) ?: getString(Res.string.common_unknown),
            )
        BackupManifestValidationError.MISSING_APP_VERSION ->
            getString(Res.string.setting_backup_error_app_version)
        BackupManifestValidationError.NEWER_APP_VERSION ->
            getString(Res.string.setting_backup_error_newer_app)
        BackupManifestValidationError.MISSING_CORE_VERSION ->
            getString(Res.string.setting_backup_error_core_version)
    }
}
