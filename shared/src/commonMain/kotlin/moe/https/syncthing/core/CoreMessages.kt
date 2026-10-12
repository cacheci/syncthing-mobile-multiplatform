package moe.https.syncthing.core

import moe.https.syncthing.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

enum class CoreMessageKey(
    val resource: StringResource,
    val diagnosticFormat: String,
) {
    CORE_IMPORT_DIRECTORY(
        Res.string.core_error_core_import_directory,
        "Could not create the core import temporary directory",
    ),
    CORE_READ_SELECTED(Res.string.core_error_core_read_selected, "Could not read the selected core file"),
    CORE_CREATE_EXTERNAL_DIRECTORY(
        Res.string.core_error_core_create_external_directory,
        "Could not create the external core directory",
    ),
    CORE_EXTERNAL_MISSING(Res.string.core_error_core_external_missing, "The selected external core does not exist"),
    CORE_EXTERNAL_DIRECTORY_INVALID(
        Res.string.core_error_core_external_directory_invalid,
        "The external core directory is invalid",
    ),
    CORE_EXTERNAL_DELETE_OUTSIDE(
        Res.string.core_error_core_external_delete_outside,
        "Refusing to delete files outside the external core directory",
    ),
    CORE_EXTERNAL_READ_DIRECTORY(
        Res.string.core_error_core_external_read_directory,
        "Could not read the external core directory",
    ),
    CORE_EXTERNAL_UNKNOWN_FILES(
        Res.string.core_error_core_external_unknown_files,
        "The external core directory contains unknown files; deletion was cancelled",
    ),
    CORE_EXTERNAL_DELETE_FILE(
        Res.string.core_error_core_external_delete_file,
        "Could not delete external core file: %1\$s",
    ),
    CORE_EXTERNAL_DELETE_DIRECTORY(
        Res.string.core_error_core_external_delete_directory,
        "Could not delete the external core directory",
    ),
    CORE_ELF_TOO_SHORT(Res.string.core_error_core_elf_too_short, "The core file is too short to be a valid ELF file"),
    CORE_ELF_INVALID(Res.string.core_error_core_elf_invalid, "The selected file is not an ELF executable"),
    CORE_ELF_ENDIAN(Res.string.core_error_core_elf_endian, "The core must be a little-endian ELF file"),
    CORE_ELF_ARCHITECTURE(Res.string.core_error_core_elf_architecture, "The core architecture is not supported"),
    CORE_ELF_INCOMPATIBLE(
        Res.string.core_error_core_elf_incompatible,
        "Core architecture %1\$s is incompatible with this device",
    ),
    CORE_EXTERNAL_EXECUTABLE_PERMISSION(
        Res.string.core_error_core_external_executable_permission,
        "Could not set external core executable permissions",
    ),
    CORE_EXTERNAL_EXECUTION_UNSUPPORTED(
        Res.string.core_error_core_external_execution_unsupported,
        "This Android system does not allow external cores in the app private directory; use the built-in core",
    ),
    CORE_EXTERNAL_VERSION_TIMEOUT(
        Res.string.core_error_core_external_version_timeout,
        "The external core version check timed out",
    ),
    CORE_EXTERNAL_VERSION_INVALID(
        Res.string.core_error_core_external_version_invalid,
        "The selected file did not return valid Syncthing version information",
    ),
    UNKNOWN_ERROR(Res.string.core_error_unknown_error, "Unknown error"),
    CORE_EXTERNAL_RUN_FAILED(Res.string.core_error_core_external_run_failed, "Could not run the external core: %1\$s"),
    CORE_BUILT_IN_MISSING(Res.string.core_error_core_built_in_missing, "The selected built-in core does not exist"),
    CORE_BUILT_IN_BINARY_MISSING(
        Res.string.core_error_core_built_in_binary_missing,
        "The built-in Syncthing core was not found in the APK installation directory: %1\$s",
    ),
    CORE_BUILT_IN_NOT_EXECUTABLE(
        Res.string.core_error_core_built_in_not_executable,
        "The built-in Syncthing core in the APK installation directory is not executable",
    ),
    CORE_BUILT_IN_DELETE(Res.string.core_error_core_built_in_delete, "Built-in cores cannot be deleted"),
    CORE_SELECTED_DELETE(
        Res.string.core_error_core_selected_delete,
        "The selected core cannot be deleted; select another core first",
    ),
    CORE_EXTERNAL_SELECTED_UNSUPPORTED(
        Res.string.core_error_core_external_selected_unsupported,
        "This system does not allow the selected external core to run; use the built-in core",
    ),
    PROCESS_WRITE_OUTPUT(Res.string.core_error_process_write_output, "Could not write process output: %1\$s"),
    CONFIG_ROOT_MISSING(
        Res.string.core_error_config_root_missing,
        "The Syncthing configuration file is missing the configuration root element",
    ),
    CONFIG_GUI_MISSING(
        Res.string.core_error_config_gui_missing,
        "The Syncthing configuration file is missing the gui element",
    ),
    CONFIG_OPTIONS_MISSING(
        Res.string.core_error_config_options_missing,
        "The Syncthing configuration file is missing the options element",
    ),
    CONFIG_DIRECTORY_MISSING(
        Res.string.core_error_config_directory_missing,
        "The Syncthing configuration directory does not exist",
    ),
    BACKUP_CLEANUP_IMPORTED(
        Res.string.core_error_backup_cleanup_imported,
        "Could not clean up the imported configuration directory",
    ),
    BACKUP_ORIGINAL_DIRECTORY_MISSING(
        Res.string.core_error_backup_original_directory_missing,
        "The original configuration backup directory does not exist",
    ),
    BACKUP_RECOVERY_INCOMPLETE_ORIGINAL_RETAINED(
        Res.string.core_error_backup_recovery_incomplete_original_retained,
        "Import failed and automatic recovery is incomplete; the original configuration is retained at %1\$s",
    ),
    BACKUP_RECOVERY_INCOMPLETE_ORIGINAL_MISSING(
        Res.string.core_error_backup_recovery_incomplete_original_missing,
        "Import failed and automatic recovery is incomplete; the original backup directory could not be found at %1\$s",
    ),
    BACKUP_RECOVERY_SETTINGS_INCOMPLETE(
        Res.string.core_error_backup_recovery_settings_incomplete,
        "Import failed; the original configuration directory was restored to %1\$s, but settings or runtime recovery is incomplete",
    ),
    BACKUP_RECOVERY_NO_ORIGINAL(
        Res.string.core_error_backup_recovery_no_original,
        "Import failed and automatic recovery is incomplete; no original configuration directory existed before import",
    ),
    BACKUP_ORIGINAL_UNCHANGED(
        Res.string.core_error_backup_original_unchanged,
        "Import failed; the original configuration directory was not replaced",
    ),
    BACKUP_ORIGINAL_RESTORED(
        Res.string.core_error_backup_original_restored,
        "Import failed; the original configuration was restored",
    ),
    BACKUP_IMPORT_REVERTED(
        Res.string.core_error_backup_import_reverted,
        "Import failed; the import was reverted, and no original configuration directory existed before import",
    ),
    BACKUP_OPERATION_DETAIL(Res.string.core_error_backup_operation_detail, "%1\$s: %2\$s"),
    BACKUP_REQUIRED_FILE(
        Res.string.core_error_backup_required_file,
        "The core backup is missing a required file: %1\$s",
    ),
    BACKUP_OPEN_TARGET(Res.string.core_error_backup_open_target, "Could not open the backup destination file"),
    BACKUP_SIZE_LIMIT(Res.string.core_error_backup_size_limit, "The backup file exceeds the allowed size"),
    BACKUP_OPEN_SOURCE(Res.string.core_error_backup_open_source, "Could not open the backup file"),
    BACKUP_INVALID_ZIP(Res.string.core_error_backup_invalid_zip, "The selected file is not a valid ZIP backup"),
    BACKUP_PASSWORD_REQUIRED(
        Res.string.core_error_backup_password_required,
        "The backup is encrypted; enter the backup password",
    ),
    BACKUP_READ_FAILED(Res.string.core_error_backup_read_failed, "Could not read the backup: %1\$s"),
    BACKUP_LEGACY_ENTRY(
        Res.string.core_error_backup_legacy_entry,
        "A current-format backup cannot be imported through the legacy import entry",
    ),
    BACKUP_TOO_MANY_FILES(Res.string.core_error_backup_too_many_files, "The backup contains too many files"),
    BACKUP_UNSAFE_PATH(
        Res.string.core_error_backup_unsafe_path,
        "The backup contains an unsafe or duplicate file path: %1\$s",
    ),
    BACKUP_INVALID_REQUIRED_FILE(
        Res.string.core_error_backup_invalid_required_file,
        "The backup is missing a valid %1\$s",
    ),
    BACKUP_CORE_RUNNING(
        Res.string.core_error_backup_core_running,
        "The core is still running; the backup operation cannot proceed",
    ),
    BACKUP_STOP_TIMEOUT(
        Res.string.core_error_backup_stop_timeout,
        "Waiting for the core to stop timed out; the backup operation was cancelled",
    ),
    BACKUP_PATH_MISSING(Res.string.core_error_backup_path_missing, "The backup is missing %1\$s"),
    BACKUP_PATH_INVALID(Res.string.core_error_backup_path_invalid, "The backup entry %1\$s is invalid"),
    BACKUP_EXPANDED_TOO_LARGE(
        Res.string.core_error_backup_expanded_too_large,
        "The backup entry %1\$s is too large after extraction",
    ),
    BACKUP_SETTINGS_INVALID(Res.string.core_error_backup_settings_invalid, "The app settings format is invalid"),
    BACKUP_SETTING_TYPE_INVALID(
        Res.string.core_error_backup_setting_type_invalid,
        "The type of app setting %1\$s is invalid",
    ),
    BACKUP_WRITE_SETTINGS(Res.string.core_error_backup_write_settings, "Could not write app settings"),
    FOLDER_CONFIGURATION_MISSING(
        Res.string.core_error_folder_configuration_missing,
        "The folder configuration to delete was not found: %1\$s",
    ),
    FOLDER_LOCAL_DELETE_FAILED(
        Res.string.core_error_folder_local_delete_failed,
        "The Syncthing folder configuration was removed, but deleting local files failed: %1\$s",
    ),
    SETTINGS_REST_NOT_READY(
        Res.string.core_error_settings_rest_not_ready,
        "The Syncthing core process is still running; settings can only be changed after the REST API is ready",
    ),
    DISCOVERY_HOST_REQUIRED(
        Res.string.core_error_discovery_host_required,
        "The discovery address is missing a hostname",
    ),
    DISCOVERY_PORT_REQUIRED(
        Res.string.core_error_discovery_port_required,
        "The discovery address is missing a valid port",
    ),
    SETTINGS_CONFIG_CORE_RUNNING(
        Res.string.core_error_settings_config_core_running,
        "The Syncthing core process is still running; the configuration file cannot be written concurrently",
    ),
    REST_API_KEY_MISSING(Res.string.core_error_rest_api_key_missing, "The REST API key does not exist"),
    CORE_GENERATE_TIMEOUT(
        Res.string.core_error_core_generate_timeout,
        "Generating the initial Syncthing configuration timed out",
    ),
    CORE_GENERATE_FAILED(
        Res.string.core_error_core_generate_failed,
        "Generating the initial Syncthing configuration failed with exit code %1\$s",
    ),
    TLS_CERTIFICATE_MISSING(
        Res.string.core_error_tls_certificate_missing,
        "The HTTPS certificate does not exist: %1\$s",
    ),
    TLS_FILE_EMPTY(Res.string.core_error_tls_file_empty, "The %1\$s file is empty"),
    TLS_FILE_TOO_LARGE(Res.string.core_error_tls_file_too_large, "The %1\$s file must not exceed 1 MiB"),
    TLS_CERTIFICATE_PEM_INVALID(
        Res.string.core_error_tls_certificate_pem_invalid,
        "The selected file is not a PEM-encoded X.509 certificate",
    ),
    TLS_CERTIFICATE_EMPTY(
        Res.string.core_error_tls_certificate_empty,
        "The certificate file contains no valid certificates",
    ),
    TLS_CERTIFICATE_PARSE_FAILED(
        Res.string.core_error_tls_certificate_parse_failed,
        "Could not parse the X.509 certificate",
    ),
    TLS_PRIVATE_KEY_INVALID(
        Res.string.core_error_tls_private_key_invalid,
        "The selected file is not a supported PEM private key",
    ),
    GUI_PORTS_UNAVAILABLE(
        Res.string.core_error_gui_ports_unavailable,
        "GUI port %1\$s and the following %2\$s ports are unavailable",
    ),
    REST_LOCAL_DEVICE_ID_MISSING(
        Res.string.core_error_rest_local_device_id_missing,
        "The Syncthing REST status is missing the local device ID",
    ),
    FOLDER_PATH_EMPTY(
        Res.string.core_error_folder_path_empty,
        "The folder path is empty; refusing to delete local files",
    ),
    FOLDER_PATH_UNRESOLVED(Res.string.core_error_folder_path_unresolved, "Could not resolve the folder path: %1\$s"),
    FOLDER_PATH_NOT_ABSOLUTE(Res.string.core_error_folder_path_not_absolute, "The folder path is not absolute: %1\$s"),
    FOLDER_PATH_SYMLINK(
        Res.string.core_error_folder_path_symlink,
        "The folder path is a symbolic link; refusing to delete local files: %1\$s",
    ),
    FOLDER_PATH_OUTSIDE_ALLOWED(
        Res.string.core_error_folder_path_outside_allowed,
        "The folder path is outside the allowed deletion directories: %1\$s",
    ),
    FOLDER_PUBLIC_ROOT_DELETE(
        Res.string.core_error_folder_public_root_delete,
        "Refusing to delete the public storage root: %1\$s",
    ),
    FOLDER_PATH_OVERLAP(
        Res.string.core_error_folder_path_overlap,
        "The path overlaps another sync folder; refusing to delete local files: %1\$s",
    ),
    FOLDER_PATH_NOT_DIRECTORY(
        Res.string.core_error_folder_path_not_directory,
        "The folder path is not a directory: %1\$s",
    ),
    TLS_CERTIFICATE_NAME(Res.string.core_error_tls_certificate_name, "HTTPS certificate"),
    TLS_PRIVATE_KEY_NAME(Res.string.core_error_tls_private_key_name, "HTTPS certificate private key"),
    REST_PING_MISSING(Res.string.core_error_rest_ping_missing, "The response is missing the ping field"),
    REST_PING_INVALID(Res.string.core_error_rest_ping_invalid, "Invalid Syncthing REST ping response: %1\$s"),
    REST_HTTP_ERROR(Res.string.core_error_rest_http_error, "Syncthing REST %1\$s %2\$s returned HTTP %3\$s%4\$s%5\$s"),
    REST_CONNECTION_UNKNOWN(Res.string.core_error_rest_connection_unknown, "Unknown REST connection error"),
    FOLDER_SCAN_TIMEOUT(
        Res.string.folder_scan_timeout,
        "Timed out waiting for the scan result. Refresh to check its status",
    ),
    REST_NO_RESPONSE(Res.string.core_error_rest_no_response, "No REST response received"),
    SPACE_DETAIL(Res.string.core_error_space_detail, " %1\$s"),
    COLON_DETAIL(Res.string.core_error_colon_detail, ": %1\$s"),
}

class CoreMessage(
    private val key: CoreMessageKey,
    private vararg val arguments: Any,
) {
    fun diagnostic(): String = DIAGNOSTIC_ARGUMENT.replace(key.diagnosticFormat) { match ->
        when (val argument = arguments[match.groupValues[1].toInt() - 1]) {
            is CoreMessage -> argument.diagnostic()
            is Throwable -> argument.message?.takeIf(String::isNotBlank) ?: argument::class.simpleName.orEmpty()
            else -> argument.toString()
        }
    }

    suspend fun localized(): String {
        val values = arguments.map { argument ->
            when (argument) {
                is CoreMessage -> argument.localized()
                is Throwable -> argument.localizedUserMessageOrNull() ?: argument::class.simpleName.orEmpty()
                else -> argument
            }
        }
        return getString(key.resource, *values.toTypedArray())
    }
}

private val DIAGNOSTIC_ARGUMENT = Regex("%([1-9][0-9]*)\\\$s")
