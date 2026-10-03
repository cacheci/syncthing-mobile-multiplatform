package moe.https.syncthing.viewmodel

import moe.https.syncthing.core.SettingAccessMode
import moe.https.syncthing.core.SettingConfiguration
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.setting_error_connection_limit_range
import moe.https.syncthing.generated.resources.setting_error_device_name_required
import moe.https.syncthing.generated.resources.setting_error_discovery_port_integer
import moe.https.syncthing.generated.resources.setting_error_discovery_port_range
import moe.https.syncthing.generated.resources.setting_error_disk_space_nonnegative
import moe.https.syncthing.generated.resources.setting_error_disk_space_number
import moe.https.syncthing.generated.resources.setting_error_disk_space_percent
import moe.https.syncthing.generated.resources.setting_error_download_limit_integer
import moe.https.syncthing.generated.resources.setting_error_enough_connections
import moe.https.syncthing.generated.resources.setting_error_lan_subnet_format
import moe.https.syncthing.generated.resources.setting_error_max_connections_integer
import moe.https.syncthing.generated.resources.setting_error_multicast_address_format
import moe.https.syncthing.generated.resources.setting_error_password_length
import moe.https.syncthing.generated.resources.setting_error_password_required
import moe.https.syncthing.generated.resources.setting_error_password_whitespace
import moe.https.syncthing.generated.resources.setting_error_rate_limits_nonnegative
import moe.https.syncthing.generated.resources.setting_error_reconnect_integer
import moe.https.syncthing.generated.resources.setting_error_reconnect_nonnegative
import moe.https.syncthing.generated.resources.setting_error_settings_not_loaded
import moe.https.syncthing.generated.resources.setting_error_upload_limit_integer
import moe.https.syncthing.generated.resources.setting_error_username_required
import moe.https.syncthing.generated.resources.setting_error_webui_port_integer
import moe.https.syncthing.generated.resources.setting_error_webui_port_range
import moe.https.syncthing.ui.model.SettingFormState
import moe.https.syncthing.ui.model.SettingUiState
import org.jetbrains.compose.resources.StringResource

internal enum class SettingEditField {
    DEVICE_NAME, DISK_SPACE, GUI_PORT, GUI_USER, GUI_PASSWORD,
    UPLOAD_LIMIT, DOWNLOAD_LIMIT, RECONNECT_INTERVAL, DISCOVERY_PORT,
    DISCOVERY_ADDRESS, LAN_SUBNETS, MAX_CONNECTIONS,
}

internal data class SettingEditValidation(
    val error: StringResource? = null,
    val canSubmit: Boolean = false,
)

internal fun validateSettingEdit(
    state: SettingUiState,
    field: SettingEditField,
    value: String,
    unit: SettingConfiguration.DiskSpaceUnit = state.formState.minHomeDiskFreeUnit,
): SettingEditValidation {
    val setting = state.settingRaw ?: return SettingEditValidation()
    val candidate = field.applyTo(state.formState, value, unit)
    val error = settingFieldError(field, candidate, setting)
    val available = state.accessMode != null && !state.isLoading && !state.isSaving &&
        (field == SettingEditField.GUI_PORT || state.accessMode != SettingAccessMode.STARTUP_ONLY) &&
        (field !in listOf(SettingEditField.GUI_USER, SettingEditField.GUI_PASSWORD) ||
            state.formState.guiAuthenticationEnabled)
    val changed = candidate.toConfiguration(setting).trim() != state.formState.toConfiguration(setting).trim() &&
        (field != SettingEditField.GUI_PASSWORD || value.isNotEmpty())
    return SettingEditValidation(error, available && error == null && changed)
}

internal fun SettingEditField.applyTo(
    form: SettingFormState,
    value: String,
    unit: SettingConfiguration.DiskSpaceUnit = form.minHomeDiskFreeUnit,
): SettingFormState = when (this) {
    SettingEditField.DEVICE_NAME -> form.copy(deviceName = value)
    SettingEditField.DISK_SPACE -> form.copy(minHomeDiskFree = value, minHomeDiskFreeUnit = unit)
    SettingEditField.GUI_PORT -> form.copy(guiPort = value)
    SettingEditField.GUI_USER -> form.copy(guiUser = value)
    SettingEditField.GUI_PASSWORD -> form.copy(newGuiPassword = value)
    SettingEditField.UPLOAD_LIMIT -> form.copy(maxSendKiBPerSecond = value)
    SettingEditField.DOWNLOAD_LIMIT -> form.copy(maxReceiveKiBPerSecond = value)
    SettingEditField.RECONNECT_INTERVAL -> form.copy(reconnectionIntervalSeconds = value)
    SettingEditField.DISCOVERY_PORT -> form.copy(localDiscoveryPort = value)
    SettingEditField.DISCOVERY_ADDRESS -> form.copy(localDiscoveryMulticastAddress = value)
    SettingEditField.LAN_SUBNETS -> form.copy(alwaysLocalNetworks = value)
    SettingEditField.MAX_CONNECTIONS -> form.copy(connectionLimitMax = value)
}

internal fun SettingFormState.validationError(
    setting: SettingConfiguration,
    accessMode: SettingAccessMode?,
): StringResource? {
    if (accessMode == null) return Res.string.setting_error_settings_not_loaded
    settingFieldError(SettingEditField.GUI_PORT, this, setting)?.let { return it }
    if (accessMode == SettingAccessMode.STARTUP_ONLY) return null
    for (field in SettingEditField.entries) {
        if (field in listOf(SettingEditField.GUI_USER, SettingEditField.GUI_PASSWORD) && !guiAuthenticationEnabled) {
            continue
        }
        // Existing address entries must not block unrelated edits; validate replacements when submitted.
        if (field == SettingEditField.DISCOVERY_ADDRESS &&
            toConfiguration(setting).trim().localDiscoveryMulticastAddress == setting.localDiscoveryMulticastAddress
        ) continue
        if (field == SettingEditField.LAN_SUBNETS && subnetValues(alwaysLocalNetworks) == setting.alwaysLocalNetworks) {
            continue
        }
        settingFieldError(field, this, setting)?.let { return it }
    }
    return null
}

internal fun SettingFormState.settingChangeError(
    setting: SettingConfiguration,
    accessMode: SettingAccessMode?,
    fields: Set<SettingSaveField>,
): StringResource? {
    if (accessMode == null) return Res.string.setting_error_settings_not_loaded
    val editableFields = buildList {
        if (SettingSaveField.GUI_PORT in fields) add(SettingEditField.GUI_PORT)
        if (accessMode == SettingAccessMode.STARTUP_ONLY) return@buildList
        if (SettingSaveField.DEVICE_NAME in fields) add(SettingEditField.DEVICE_NAME)
        if (SettingSaveField.DISK_SPACE in fields) add(SettingEditField.DISK_SPACE)
        if (SettingSaveField.AUTHENTICATION in fields && guiAuthenticationEnabled) {
            add(SettingEditField.GUI_USER)
            add(SettingEditField.GUI_PASSWORD)
        }
        if (SettingSaveField.UPLOAD_LIMIT in fields) add(SettingEditField.UPLOAD_LIMIT)
        if (SettingSaveField.DOWNLOAD_LIMIT in fields) add(SettingEditField.DOWNLOAD_LIMIT)
        if (SettingSaveField.RECONNECT_INTERVAL in fields) add(SettingEditField.RECONNECT_INTERVAL)
        if (SettingSaveField.DISCOVERY_PORT in fields) add(SettingEditField.DISCOVERY_PORT)
        if (SettingSaveField.DISCOVERY_MULTICAST in fields) add(SettingEditField.DISCOVERY_ADDRESS)
        if (SettingSaveField.LAN_SUBNETS in fields) add(SettingEditField.LAN_SUBNETS)
        if (SettingSaveField.MAX_CONNECTIONS in fields) add(SettingEditField.MAX_CONNECTIONS)
    }
    return editableFields.firstNotNullOfOrNull { settingFieldError(it, this, setting) }
}

private fun settingFieldError(
    field: SettingEditField,
    form: SettingFormState,
    setting: SettingConfiguration,
): StringResource? {
    val defaults = SettingConfiguration.startupDefaults()
    return when (field) {
        SettingEditField.DEVICE_NAME -> if (form.deviceName.isBlank()) Res.string.setting_error_device_name_required else null
        SettingEditField.GUI_USER -> if (form.guiUser.isBlank()) Res.string.setting_error_username_required else null
        SettingEditField.DISK_SPACE -> {
            val value = form.minHomeDiskFree
            val number = if (value.isBlank()) defaults.minHomeDiskFree else value.toDoubleOrNull()
            when {
                number == null -> Res.string.setting_error_disk_space_number
                !number.isFinite() || number < 0 -> Res.string.setting_error_disk_space_nonnegative
                form.minHomeDiskFreeUnit == SettingConfiguration.DiskSpaceUnit.PERCENT && number > 100 ->
                    Res.string.setting_error_disk_space_percent
                else -> null
            }
        }
        SettingEditField.GUI_PORT -> integerError(
            form.guiPort, defaults.guiPort, 1..65535,
            Res.string.setting_error_webui_port_integer, Res.string.setting_error_webui_port_range,
        )
        SettingEditField.GUI_PASSWORD -> when {
            form.newGuiPassword.isEmpty() && !setting.guiPasswordConfigured -> Res.string.setting_error_password_required
            form.newGuiPassword.isNotEmpty() && form.newGuiPassword.isBlank() -> Res.string.setting_error_password_whitespace
            form.newGuiPassword.encodeToByteArray().size > 72 -> Res.string.setting_error_password_length
            else -> null
        }
        SettingEditField.UPLOAD_LIMIT -> integerError(
            form.maxSendKiBPerSecond, defaults.maxSendKiBPerSecond, 0..Int.MAX_VALUE,
            Res.string.setting_error_upload_limit_integer, Res.string.setting_error_rate_limits_nonnegative,
        )
        SettingEditField.DOWNLOAD_LIMIT -> integerError(
            form.maxReceiveKiBPerSecond, defaults.maxReceiveKiBPerSecond, 0..Int.MAX_VALUE,
            Res.string.setting_error_download_limit_integer, Res.string.setting_error_rate_limits_nonnegative,
        )
        SettingEditField.RECONNECT_INTERVAL -> integerError(
            form.reconnectionIntervalSeconds, defaults.reconnectionIntervalSeconds, 0..Int.MAX_VALUE,
            Res.string.setting_error_reconnect_integer, Res.string.setting_error_reconnect_nonnegative,
        )
        SettingEditField.DISCOVERY_PORT -> integerError(
            form.localDiscoveryPort, defaults.localDiscoveryPort, 1..65535,
            Res.string.setting_error_discovery_port_integer, Res.string.setting_error_discovery_port_range,
        )
        SettingEditField.MAX_CONNECTIONS -> integerError(
            form.connectionLimitMax, defaults.connectionLimitMax, 0..1023,
            Res.string.setting_error_max_connections_integer, Res.string.setting_error_connection_limit_range,
        ) ?: when {
            setting.connectionLimitEnough !in 0..1023 -> Res.string.setting_error_connection_limit_range
            (form.connectionLimitMax.toIntOrNull() ?: defaults.connectionLimitMax) in 1..<setting.connectionLimitEnough ->
                Res.string.setting_error_enough_connections
            else -> null
        }
        SettingEditField.DISCOVERY_ADDRESS -> if (form.localDiscoveryMulticastAddress.isNotBlank() &&
            !isValidDiscoveryEndpoint(form.localDiscoveryMulticastAddress.trim())
        ) Res.string.setting_error_multicast_address_format else null
        SettingEditField.LAN_SUBNETS -> if (subnetValues(form.alwaysLocalNetworks).any { !isValidCidr(it) }) {
            Res.string.setting_error_lan_subnet_format
        } else null
    }
}

private fun integerError(
    value: String,
    defaultValue: Int,
    range: IntRange,
    integerError: StringResource,
    rangeError: StringResource,
): StringResource? {
    val number = if (value.isBlank()) defaultValue else value.toIntOrNull()
    return when {
        number == null -> integerError
        number !in range -> rangeError
        else -> null
    }
}

private fun subnetValues(value: String): List<String> =
    value.split(',', '\n').map(String::trim).filter(String::isNotBlank).distinct()

internal fun isValidCidr(value: String): Boolean {
    val parts = value.split('/')
    if (parts.size != 2 || parts[1].isEmpty() || parts[1].any { it !in '0'..'9' }) return false
    val prefix = parts[1].toIntOrNull() ?: return false
    return when {
        isValidIpv4(parts[0]) -> prefix in 0..32
        isValidIpv6(parts[0]) -> prefix in 0..128
        else -> false
    }
}

internal fun isValidDiscoveryEndpoint(value: String): Boolean {
    val port: String
    if (value.startsWith('[')) {
        val closing = value.indexOf(']')
        if (closing < 0 || value.getOrNull(closing + 1) != ':') return false
        val host = value.substring(1, closing)
        val address = host.substringBefore('%')
        val zone = host.substringAfter('%', "")
        if (!isValidIpv6(address) || ('%' in host &&
                (zone.isEmpty() || zone.any { it.isWhitespace() || it in "%[]/:" }))
        ) return false
        port = value.substring(closing + 2)
    } else {
        if (value.count { it == ':' } != 1) return false
        val host = value.substringBefore(':')
        // Core also accepts host names and an empty host for local broadcast.
        if (host.any { it.isWhitespace() || it in "[]/\\@?#%" }) return false
        if (host.isNotEmpty() && host.all { it in "0123456789." } && !isValidIpv4(host)) return false
        port = value.substringAfter(':')
    }
    return port.isNotEmpty() && port.all { it in '0'..'9' } && port.toIntOrNull()?.let { it in 1..65535 } == true
}

private fun isValidIpv4(value: String): Boolean {
    val parts = value.split('.')
    return parts.size == 4 && parts.all { part ->
        part.isNotEmpty() && part.all { it in '0'..'9' } &&
            (part.length == 1 || part.first() != '0') && part.toIntOrNull()?.let { it in 0..255 } == true
    }
}

private fun isValidIpv6(value: String): Boolean {
    if (value.isEmpty() || ":::" in value || '%' in value) return false
    val compressed = "::" in value
    if (compressed && value.indexOf("::") != value.lastIndexOf("::")) return false
    if (value.startsWith(':') && !value.startsWith("::")) return false
    if (value.endsWith(':') && !value.endsWith("::")) return false
    val parts = value.split(':').filter(String::isNotEmpty)
    var groups = 0
    for ((index, part) in parts.withIndex()) {
        if ('.' in part) {
            if (index != parts.lastIndex || !value.endsWith(part) || !isValidIpv4(part)) return false
            groups += 2
        } else {
            if (part.length !in 1..4 || part.any { it !in "0123456789abcdefABCDEF" }) return false
            groups++
        }
    }
    return if (compressed) groups < 8 else groups == 8
}
