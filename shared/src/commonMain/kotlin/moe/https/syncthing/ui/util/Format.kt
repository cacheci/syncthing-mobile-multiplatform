package moe.https.syncthing.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.https.syncthing.core.CoreState
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun CoreState.displayName(): String = when (this) {
    CoreState.NOT_INSTALLED -> stringResource(Res.string.common_not_installed)
    CoreState.STOPPED -> stringResource(Res.string.common_state_core_stopped)
    CoreState.INSTALLING -> stringResource(Res.string.common_state_core_importing)
    CoreState.STARTING -> stringResource(Res.string.common_state_core_starting)
    CoreState.RUNNING -> stringResource(Res.string.common_state_core_running)
    CoreState.STOPPING -> stringResource(Res.string.common_state_core_stopping)
    CoreState.FAILED -> stringResource(Res.string.common_state_core_failed)
}

internal fun formatBytes(value: Long?): String? {
    if (value == null) return null
    val units = listOf("B", "KiB", "MiB", "GiB")
    var number = value.toDouble()
    var unit = 0
    while (number >= 1024 && unit < units.lastIndex) {
        number /= 1024
        unit++
    }
    return if (unit == 0) {
        "${number.toLong()} ${units[unit]}"
    } else {
        "${(number * 10).toLong() / 10.0} ${units[unit]}"
    }
}

internal fun formatBitsPerSecond(bytesPerSecond: Long?): String? {
    if (bytesPerSecond == null) return null
    val units = listOf("bps", "kbps", "Mbps", "Gbps", "Tbps")
    var number = bytesPerSecond.toDouble() * 8.0
    var unit = 0
    while (number > 1_000.0 && unit < units.lastIndex) {
        number /= 1_000.0
        unit++
    }
    val rounded = when {
        unit == 0 || number >= 100.0 -> round(number)
        number >= 10.0 -> round(number * 10.0) / 10.0
        else -> round(number * 100.0) / 100.0
    }
    val displayValue = if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
    return "$displayValue ${units[unit]}"
}

@Composable
internal fun formatDuration(seconds: Long?): String? {
    if (seconds == null) return null
    val days = seconds / 86_400
    val hours = seconds % 86_400 / 3_600
    val minutes = seconds % 3_600 / 60
    val remainingSeconds = seconds % 60
    val dayText = stringResource(Res.string.common_duration_days, days)
    val hourText = stringResource(Res.string.common_duration_hours, hours)
    val minuteText = stringResource(Res.string.common_duration_minutes, minutes)
    val secondText = stringResource(Res.string.common_duration_seconds, remainingSeconds)
    return buildString {
        if (days > 0) append(dayText).append(' ')
        if (hours > 0 || days > 0) append(hourText).append(' ')
        if (minutes > 0 || hours > 0 || days > 0) append(minuteText).append(' ')
        append(secondText)
    }
}

@Composable
internal fun countToColouredString(succeeded: Int, total: Int ): Pair<String, Color> {
    if (total == 0) return "—" to AppTheme.colorScheme.onBackground

    return stringResource(Res.string.common_online_count, succeeded, total) to when (succeeded) {
        total -> AppTheme.statusColors.ok
        0 -> AppTheme.statusColors.fail
        else -> AppTheme.statusColors.pending
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
internal fun Instant.toReadable(
    defaultTakePlace: String = "",
): String {
    val timeZoneSystem = TimeZone.currentSystemDefault()
    val nowDateTime = Clock.System.now().toLocalDateTime(timeZoneSystem)
    val dateTime = this.toLocalDateTime(timeZoneSystem)

    if (dateTime.year < 1970) return defaultTakePlace
    if (nowDateTime.year != dateTime.year) return LocalDateTime.Format { byUnicodePattern("yyyy/MM/dd HH:mm") }.format(dateTime)
    if (nowDateTime.dayOfYear != dateTime.dayOfYear) return LocalDateTime.Format { byUnicodePattern("MM/dd HH:mm") }.format(dateTime)
    return LocalDateTime.Format { byUnicodePattern("HH:mm") }.format(dateTime)
}

@Serializable
data class ListenAddressListItem(
    val enabled: Boolean,
    val uri: String
)

@Serializable
data class ListenAddressSetting(
    @SerialName("STACK")
    val stackPrefer: UriProtocolStack,

    @SerialName("TCP")
    val tcp: Boolean = true,

    @SerialName("QUIC")
    val quic: Boolean = true,

    @SerialName("PORT")
    val port: Int,

    @SerialName("relay")
    val relays: List<ListenAddressListItem> = emptyList(),
)

enum class SettingProtocolStack(val guiListenAddress: String) {
    IPV4("127.0.0.1"), IPV6("::1"), DUAL("localhost"), CUSTOM("localhost")
}

enum class UriProtocolStack {
    IPV4, IPV6, DUAL,
}
