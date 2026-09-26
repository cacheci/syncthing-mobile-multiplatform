package moe.https.syncthing.ui.util

import androidx.compose.runtime.Composable
import moe.https.syncthing.core.NewFolderConfiguration
import moe.https.syncthing.core.SettingAccessMode
import moe.https.syncthing.core.SettingConfiguration
import moe.https.syncthing.generated.resources.*
import moe.https.syncthing.ui.model.AppPage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal val AppPage.titleResource: StringResource
    get() = when (this) {
        AppPage.DEVICES -> Res.string.device_page_connections
        AppPage.FOLDERS -> Res.string.folder_page_folders
        AppPage.CORE -> Res.string.core_page_home
        AppPage.WEBUI -> Res.string.webui_page_webui
        AppPage.RECENT_CHANGES -> Res.string.recent_page_recent_changes
        AppPage.SETTINGS -> Res.string.setting_page_settings
    }

@Composable
internal fun AppPage.localizedTitle(): String = stringResource(titleResource)

@Composable
internal fun SettingAccessMode.localizedTitle(): String = stringResource(
    when (this) {
        SettingAccessMode.REST -> Res.string.setting_mode_running_title
        SettingAccessMode.CONFIG_FILE -> Res.string.setting_mode_offline_title
        SettingAccessMode.STARTUP_ONLY -> Res.string.setting_mode_uninitialized_title
    },
)

@Composable
internal fun SettingAccessMode.localizedCaption(): String = stringResource(
    when (this) {
        SettingAccessMode.REST -> Res.string.setting_mode_running_caption
        SettingAccessMode.CONFIG_FILE -> Res.string.setting_mode_offline_caption
        SettingAccessMode.STARTUP_ONLY -> Res.string.setting_mode_uninitialized_caption
    },
)

@Composable
internal fun SettingConfiguration.GuiTheme.localizedDisplayName(): String = stringResource(
    when (this) {
        SettingConfiguration.GuiTheme.DEFAULT -> Res.string.setting_theme_system
        SettingConfiguration.GuiTheme.LIGHT -> Res.string.setting_theme_light
        SettingConfiguration.GuiTheme.DARK -> Res.string.setting_theme_dark
        SettingConfiguration.GuiTheme.BLACK -> Res.string.setting_theme_black
    },
)

@Composable
internal fun SettingConfiguration.GuiPortConflictBehavior.localizedDisplayName(): String =
    stringResource(
        if (this == SettingConfiguration.GuiPortConflictBehavior.FAIL) {
            Res.string.setting_port_conflict_fail
        } else {
            Res.string.setting_port_conflict_next
        },
    )

@Composable
internal fun SettingConfiguration.RunningOnPoweredBy.localizedDisplayName(): String = stringResource(
    when (this) {
        SettingConfiguration.RunningOnPoweredBy.CHARGED -> Res.string.setting_power_ac
        SettingConfiguration.RunningOnPoweredBy.BATTERY -> Res.string.setting_power_battery
        SettingConfiguration.RunningOnPoweredBy.BOTH -> Res.string.setting_power_both
    },
)

@Composable
internal fun AutoStartModeType.localizedDisplayName(): String = stringResource(
    when (this) {
        AutoStartModeType.DISABLED -> Res.string.setting_auto_start_disabled
        AutoStartModeType.WITH_CONDITION -> Res.string.setting_auto_start_conditions
        AutoStartModeType.ENABLED -> Res.string.setting_auto_start_always
    },
)

@Composable
internal fun ExecuteScheduleType.localizedDisplayName(): String = stringResource(
    if (this == ExecuteScheduleType.INTERVAL) {
        Res.string.setting_schedule_interval
    } else {
        Res.string.setting_schedule_time_range
    },
)

@Composable
internal fun SettingProtocolStack.localizedDisplayName(): String = when (this) {
    SettingProtocolStack.IPV4 -> "IPv4"
    SettingProtocolStack.IPV6 -> "IPv6"
    SettingProtocolStack.DUAL -> stringResource(Res.string.setting_protocol_dual)
    SettingProtocolStack.CUSTOM -> stringResource(Res.string.common_advanced)
}

@Composable
internal fun UriProtocolStack.localizedDisplayName(): String = when (this) {
    UriProtocolStack.IPV4 -> "IPv4"
    UriProtocolStack.IPV6 -> "IPv6"
    UriProtocolStack.DUAL -> stringResource(Res.string.setting_protocol_dual)
}

@Composable
internal fun NewFolderConfiguration.Versioning.localizedDisplayName(): String = stringResource(
    when (this) {
        NewFolderConfiguration.Versioning.NONE -> Res.string.folder_versioning_none
        NewFolderConfiguration.Versioning.TRASHCAN -> Res.string.folder_versioning_trashcan
        NewFolderConfiguration.Versioning.SIMPLE -> Res.string.folder_versioning_simple
        NewFolderConfiguration.Versioning.STAGGERED -> Res.string.folder_versioning_staggered
        NewFolderConfiguration.Versioning.EXTERNAL -> Res.string.folder_versioning_external
    },
)

@Composable
internal fun NewFolderConfiguration.PullOrder.localizedDisplayName(): String = stringResource(
    when (this) {
        NewFolderConfiguration.PullOrder.RANDOM -> Res.string.folder_pull_random
        NewFolderConfiguration.PullOrder.ALPHABETIC -> Res.string.folder_pull_alphabetic
        NewFolderConfiguration.PullOrder.SMALLEST_FIRST -> Res.string.folder_pull_smallest
        NewFolderConfiguration.PullOrder.LARGEST_FIRST -> Res.string.folder_pull_largest
        NewFolderConfiguration.PullOrder.OLDEST_FIRST -> Res.string.folder_pull_oldest
        NewFolderConfiguration.PullOrder.NEWEST_FIRST -> Res.string.folder_pull_newest
    },
)
