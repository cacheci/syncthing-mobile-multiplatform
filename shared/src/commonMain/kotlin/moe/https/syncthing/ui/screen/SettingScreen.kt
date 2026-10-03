package moe.https.syncthing.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.https.syncthing.AppSubPage
import moe.https.syncthing.core.BackupImportFormat
import moe.https.syncthing.core.CoreAvailability
import moe.https.syncthing.core.GuiTlsFile
import moe.https.syncthing.core.SettingAccessMode
import moe.https.syncthing.core.SettingConfiguration
import moe.https.syncthing.core.defaultFolderPath
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.common_action_back
import moe.https.syncthing.generated.resources.common_action_cancel
import moe.https.syncthing.generated.resources.common_action_confirm
import moe.https.syncthing.generated.resources.common_advanced
import moe.https.syncthing.generated.resources.common_connection
import moe.https.syncthing.generated.resources.common_device_name
import moe.https.syncthing.generated.resources.common_download_limit_kib
import moe.https.syncthing.generated.resources.common_failed
import moe.https.syncthing.generated.resources.common_label_device_discovery
import moe.https.syncthing.generated.resources.common_label_listen_addresses
import moe.https.syncthing.generated.resources.common_read_failed
import moe.https.syncthing.generated.resources.common_required
import moe.https.syncthing.generated.resources.common_unlimited
import moe.https.syncthing.generated.resources.common_upload_limit_kib
import moe.https.syncthing.generated.resources.setting_accessibility
import moe.https.syncthing.generated.resources.setting_add_current_wlan
import moe.https.syncthing.generated.resources.setting_add_discovery_server
import moe.https.syncthing.generated.resources.setting_add_named_item
import moe.https.syncthing.generated.resources.setting_add_relay_server
import moe.https.syncthing.generated.resources.setting_add_time_range
import moe.https.syncthing.generated.resources.setting_additional_lan_subnets
import moe.https.syncthing.generated.resources.setting_advanced
import moe.https.syncthing.generated.resources.setting_airplane_mode_network_summary
import moe.https.syncthing.generated.resources.setting_allow_app_auto_start
import moe.https.syncthing.generated.resources.setting_allow_ignore_battery_optimization
import moe.https.syncthing.generated.resources.setting_announce_lan_addresses
import moe.https.syncthing.generated.resources.setting_announce_lan_addresses_summary
import moe.https.syncthing.generated.resources.setting_authentication
import moe.https.syncthing.generated.resources.setting_authentication_password
import moe.https.syncthing.generated.resources.setting_authentication_summary
import moe.https.syncthing.generated.resources.setting_authentication_user
import moe.https.syncthing.generated.resources.setting_authorize_location_permission
import moe.https.syncthing.generated.resources.setting_auto_start
import moe.https.syncthing.generated.resources.setting_background_permission
import moe.https.syncthing.generated.resources.setting_background_permissions_message
import moe.https.syncthing.generated.resources.setting_backup_password
import moe.https.syncthing.generated.resources.setting_backup_settings
import moe.https.syncthing.generated.resources.setting_backup_working
import moe.https.syncthing.generated.resources.setting_battery_optimization_message
import moe.https.syncthing.generated.resources.setting_battery_range
import moe.https.syncthing.generated.resources.setting_bottom_bar_blur
import moe.https.syncthing.generated.resources.setting_bottom_bar_items
import moe.https.syncthing.generated.resources.setting_bottom_bar_settings
import moe.https.syncthing.generated.resources.setting_cidr_one_per_line
import moe.https.syncthing.generated.resources.setting_confirm_import
import moe.https.syncthing.generated.resources.setting_confirm_password
import moe.https.syncthing.generated.resources.setting_connection_protocol_stack
import moe.https.syncthing.generated.resources.setting_core_selection
import moe.https.syncthing.generated.resources.setting_cron_triggers
import moe.https.syncthing.generated.resources.setting_cron_triggers_summary
import moe.https.syncthing.generated.resources.setting_default_page
import moe.https.syncthing.generated.resources.setting_default_page_summary
import moe.https.syncthing.generated.resources.setting_default_path
import moe.https.syncthing.generated.resources.setting_default_relay_summary
import moe.https.syncthing.generated.resources.setting_developer_options
import moe.https.syncthing.generated.resources.setting_discovery_servers
import moe.https.syncthing.generated.resources.setting_discovery_servers_description
import moe.https.syncthing.generated.resources.setting_empty_message
import moe.https.syncthing.generated.resources.setting_empty_title
import moe.https.syncthing.generated.resources.setting_enable_location_services
import moe.https.syncthing.generated.resources.setting_every_day
import moe.https.syncthing.generated.resources.setting_every_week
import moe.https.syncthing.generated.resources.setting_export
import moe.https.syncthing.generated.resources.setting_export_backup
import moe.https.syncthing.generated.resources.setting_expression
import moe.https.syncthing.generated.resources.setting_external_path
import moe.https.syncthing.generated.resources.setting_floating_bottom_bar
import moe.https.syncthing.generated.resources.setting_follow_conditions
import moe.https.syncthing.generated.resources.setting_follow_conditions_summary
import moe.https.syncthing.generated.resources.setting_follow_network_conditions
import moe.https.syncthing.generated.resources.setting_follow_network_conditions_summary
import moe.https.syncthing.generated.resources.setting_follow_power_saver
import moe.https.syncthing.generated.resources.setting_follow_power_saver_summary
import moe.https.syncthing.generated.resources.setting_from_time
import moe.https.syncthing.generated.resources.setting_general
import moe.https.syncthing.generated.resources.setting_global_discovery
import moe.https.syncthing.generated.resources.setting_global_discovery_servers
import moe.https.syncthing.generated.resources.setting_global_discovery_summary
import moe.https.syncthing.generated.resources.setting_grant_location_permission
import moe.https.syncthing.generated.resources.setting_high_contrast_mode
import moe.https.syncthing.generated.resources.setting_ignore_battery_optimization
import moe.https.syncthing.generated.resources.setting_import
import moe.https.syncthing.generated.resources.setting_import_backup
import moe.https.syncthing.generated.resources.setting_import_backup_summary
import moe.https.syncthing.generated.resources.setting_import_core
import moe.https.syncthing.generated.resources.setting_import_from_fork
import moe.https.syncthing.generated.resources.setting_import_https_certificate
import moe.https.syncthing.generated.resources.setting_import_https_private_key
import moe.https.syncthing.generated.resources.setting_import_legacy_backup
import moe.https.syncthing.generated.resources.setting_import_overwrite_warning
import moe.https.syncthing.generated.resources.setting_internal_core
import moe.https.syncthing.generated.resources.setting_interval_schedule_summary
import moe.https.syncthing.generated.resources.setting_ipv4_multicast_port
import moe.https.syncthing.generated.resources.setting_ipv6_multicast_address
import moe.https.syncthing.generated.resources.setting_keep_backup_safe
import moe.https.syncthing.generated.resources.setting_keep_password_safe
import moe.https.syncthing.generated.resources.setting_lan_rate_limit
import moe.https.syncthing.generated.resources.setting_lan_rate_limit_summary
import moe.https.syncthing.generated.resources.setting_latency
import moe.https.syncthing.generated.resources.setting_leave_blank_if_unencrypted
import moe.https.syncthing.generated.resources.setting_legacy_import_warning
import moe.https.syncthing.generated.resources.setting_listen_protocol_stack
import moe.https.syncthing.generated.resources.setting_local_discovery
import moe.https.syncthing.generated.resources.setting_local_discovery_summary
import moe.https.syncthing.generated.resources.setting_location_permission_required
import moe.https.syncthing.generated.resources.setting_location_services_required
import moe.https.syncthing.generated.resources.setting_lock_background_process
import moe.https.syncthing.generated.resources.setting_maximum_connections
import moe.https.syncthing.generated.resources.setting_minimum_free_disk_space
import moe.https.syncthing.generated.resources.setting_mobile_data
import moe.https.syncthing.generated.resources.setting_mode_uninitialized_caption
import moe.https.syncthing.generated.resources.setting_mode_uninitialized_title
import moe.https.syncthing.generated.resources.setting_nat_traversal
import moe.https.syncthing.generated.resources.setting_nat_traversal_summary
import moe.https.syncthing.generated.resources.setting_network
import moe.https.syncthing.generated.resources.setting_not_loaded_message
import moe.https.syncthing.generated.resources.setting_not_loaded_title
import moe.https.syncthing.generated.resources.setting_not_selected
import moe.https.syncthing.generated.resources.setting_one_per_line
import moe.https.syncthing.generated.resources.setting_page_appearance
import moe.https.syncthing.generated.resources.setting_page_background_running
import moe.https.syncthing.generated.resources.setting_page_battery_conditions
import moe.https.syncthing.generated.resources.setting_page_common
import moe.https.syncthing.generated.resources.setting_page_connection
import moe.https.syncthing.generated.resources.setting_page_discovery_servers
import moe.https.syncthing.generated.resources.setting_page_listen_addresses
import moe.https.syncthing.generated.resources.setting_page_location_permission
import moe.https.syncthing.generated.resources.setting_page_network_conditions
import moe.https.syncthing.generated.resources.setting_page_storage
import moe.https.syncthing.generated.resources.setting_page_storage_permission
import moe.https.syncthing.generated.resources.setting_page_time_ranges
import moe.https.syncthing.generated.resources.setting_page_webui
import moe.https.syncthing.generated.resources.setting_password
import moe.https.syncthing.generated.resources.setting_pause_duration_minutes
import moe.https.syncthing.generated.resources.setting_port
import moe.https.syncthing.generated.resources.setting_port_increment
import moe.https.syncthing.generated.resources.setting_power_source
import moe.https.syncthing.generated.resources.setting_protocol_stack
import moe.https.syncthing.generated.resources.setting_public_storage_access
import moe.https.syncthing.generated.resources.setting_public_storage_access_message
import moe.https.syncthing.generated.resources.setting_reconnect_interval_seconds
import moe.https.syncthing.generated.resources.setting_relay_servers
import moe.https.syncthing.generated.resources.setting_restart_required_title
import moe.https.syncthing.generated.resources.setting_run_duration_minutes
import moe.https.syncthing.generated.resources.setting_run_in_battery_range
import moe.https.syncthing.generated.resources.setting_run_in_battery_range_summary
import moe.https.syncthing.generated.resources.setting_run_on_metered_wlan
import moe.https.syncthing.generated.resources.setting_run_on_mobile_data
import moe.https.syncthing.generated.resources.setting_run_on_schedule
import moe.https.syncthing.generated.resources.setting_run_on_schedule_summary
import moe.https.syncthing.generated.resources.setting_run_on_selected_wlan
import moe.https.syncthing.generated.resources.setting_run_on_wlan
import moe.https.syncthing.generated.resources.setting_run_time_ranges
import moe.https.syncthing.generated.resources.setting_run_while_roaming
import moe.https.syncthing.generated.resources.setting_run_without_network
import moe.https.syncthing.generated.resources.setting_saved_restart_required
import moe.https.syncthing.generated.resources.setting_select_end_time
import moe.https.syncthing.generated.resources.setting_select_path
import moe.https.syncthing.generated.resources.setting_select_start_time
import moe.https.syncthing.generated.resources.setting_set_backup_password
import moe.https.syncthing.generated.resources.setting_start_triggers
import moe.https.syncthing.generated.resources.setting_stop_triggers
import moe.https.syncthing.generated.resources.setting_syncthing_core
import moe.https.syncthing.generated.resources.setting_system_permissions
import moe.https.syncthing.generated.resources.setting_to_time
import moe.https.syncthing.generated.resources.setting_top_bar_blur
import moe.https.syncthing.generated.resources.setting_top_bar_settings
import moe.https.syncthing.generated.resources.setting_unavailable_message
import moe.https.syncthing.generated.resources.setting_unavailable_title
import moe.https.syncthing.generated.resources.setting_usage_reporting
import moe.https.syncthing.generated.resources.setting_usage_reporting_summary
import moe.https.syncthing.generated.resources.setting_use_https_webui
import moe.https.syncthing.generated.resources.setting_use_relays
import moe.https.syncthing.generated.resources.setting_use_relays_summary
import moe.https.syncthing.generated.resources.setting_webui_theme
import moe.https.syncthing.generated.resources.setting_weekday_fri
import moe.https.syncthing.generated.resources.setting_weekday_mon
import moe.https.syncthing.generated.resources.setting_weekday_sat
import moe.https.syncthing.generated.resources.setting_weekday_separator
import moe.https.syncthing.generated.resources.setting_weekday_sun
import moe.https.syncthing.generated.resources.setting_weekday_thu
import moe.https.syncthing.generated.resources.setting_weekday_tue
import moe.https.syncthing.generated.resources.setting_weekday_wed
import moe.https.syncthing.generated.resources.setting_wlan_names
import moe.https.syncthing.generated.resources.setting_wlan_unavailable
import moe.https.syncthing.generated.resources.setting_xiaomi
import moe.https.syncthing.generated.resources.setting_xiaomi_lock_instructions
import moe.https.syncthing.platform.FilePickerResult
import moe.https.syncthing.platform.FolderPickerResult
import moe.https.syncthing.platform.isSystem24HourFormat
import moe.https.syncthing.platform.rememberFolderPicker
import moe.https.syncthing.platform.rememberPemFilePicker
import moe.https.syncthing.ui.component.AppNavigationBar
import moe.https.syncthing.ui.component.BlurredSmallTopAppBar
import moe.https.syncthing.ui.component.CheckableInputValueRow
import moe.https.syncthing.ui.component.CheckableRow
import moe.https.syncthing.ui.component.DeleteBox
import moe.https.syncthing.ui.component.InfoSwitch
import moe.https.syncthing.ui.component.InfoSwitchCard
import moe.https.syncthing.ui.component.InputValueRow
import moe.https.syncthing.ui.component.MessageCard
import moe.https.syncthing.ui.component.TextWithOptionField
import moe.https.syncthing.ui.component.TimePicker
import moe.https.syncthing.ui.component.barBackdropSource
import moe.https.syncthing.ui.component.isBarBlurSupported
import moe.https.syncthing.ui.model.AppPage
import moe.https.syncthing.ui.model.BackupUiState
import moe.https.syncthing.ui.model.CoreUiState
import moe.https.syncthing.ui.model.MainUiState
import moe.https.syncthing.ui.model.SettingUiState
import moe.https.syncthing.ui.theme.AppTheme
import moe.https.syncthing.ui.theme.Syncthing
import moe.https.syncthing.ui.util.AutoStartModeType
import moe.https.syncthing.ui.util.BatteryRunCondition
import moe.https.syncthing.ui.util.CronTrigger
import moe.https.syncthing.ui.util.ExecuteSchedule
import moe.https.syncthing.ui.util.ExecuteScheduleType
import moe.https.syncthing.ui.util.ListenAddressListItem
import moe.https.syncthing.ui.util.NetworkRunCondition
import moe.https.syncthing.ui.util.SettingProtocolStack
import moe.https.syncthing.ui.util.UriProtocolStack
import moe.https.syncthing.ui.util.isValidCronExpression
import moe.https.syncthing.ui.util.localizedDisplayName
import moe.https.syncthing.ui.util.localizedTitle
import moe.https.syncthing.viewmodel.DiscoveryServerPingState
import moe.https.syncthing.viewmodel.SettingEditField
import moe.https.syncthing.viewmodel.SettingEditValidation
import moe.https.syncthing.viewmodel.SettingViewModel
import moe.https.syncthing.viewmodel.validateSettingEdit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextButtonColors
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.HorizontalSplit
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.RangeSliderPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun SettingScreen(
    uiState: SettingUiState,
    developerModeEnabled: Boolean,
    onNavigateTo: (AppSubPage) -> Unit,
    uiPadding: PaddingValues,
    pagePaddingHorizontal: Dp,
    modifier: Modifier = Modifier,
) {
    var developerModeVisible by remember { mutableStateOf(developerModeEnabled) }
    val settingAvailable = uiState.settingRaw != null && uiState.accessMode != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(uiPadding)
            .padding(vertical = 12.dp)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            uiState.errorMessage != null && !settingAvailable -> MessageCard(
                title = stringResource(Res.string.common_read_failed),
                message = uiState.errorMessage,
                isError = true,
            )

            uiState.hasLoaded && uiState.settingRaw == null -> MessageCard(
                title = stringResource(Res.string.setting_empty_title),
                message = stringResource(Res.string.setting_empty_message),
                isError = true,
            )

            uiState.hasLoaded && !settingAvailable -> MessageCard(
                title = stringResource(Res.string.setting_unavailable_title),
                message = stringResource(Res.string.setting_unavailable_message),
                isError = true,
            )

            !settingAvailable -> MessageCard(
                title = stringResource(Res.string.setting_not_loaded_title),
                message = stringResource(Res.string.setting_not_loaded_message),
            )

            uiState.accessMode == SettingAccessMode.STARTUP_ONLY -> MessageCard(
                title = stringResource(Res.string.setting_mode_uninitialized_title),
                message = stringResource(Res.string.setting_mode_uninitialized_caption),
            )

            uiState.restartRequired -> MessageCard(
                title = stringResource(Res.string.setting_restart_required_title),
                message = stringResource(Res.string.setting_saved_restart_required),
            )
        }

        Card {
            Column {
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.b,
                        imageVector = MiuixIcons.Settings,
                    ) },
                    title = stringResource(Res.string.setting_page_common),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_COMMON) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.d,
                        imageVector = MiuixIcons.Theme, // TODO: Change Icon
                    ) },
                    title = stringResource(Res.string.setting_page_storage),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_STORAGE) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.e,
                        imageVector = MiuixIcons.HorizontalSplit,
                    ) },
                    title = stringResource(Res.string.setting_page_webui),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_WEBUI) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.f,
                        imageVector = MiuixIcons.Link,
                    ) },
                    title = stringResource(Res.string.setting_page_connection),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_CONNECTION) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.c,
                        imageVector = MiuixIcons.Theme,
                    ) },
                    title = stringResource(Res.string.setting_page_appearance),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_THEME) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.a,
                        imageVector = MiuixIcons.Lock
                    ) },
                    title = stringResource(Res.string.setting_system_permissions),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_PERMISSIONS) },
                )
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.h,
                        imageVector = MiuixIcons.Lock // TODO: icon
                    ) },
                    title = stringResource(Res.string.setting_auto_start),
                    onClick = { onNavigateTo(AppSubPage.SETTINGS_AUTOSTART) },
                )
            }
        }

        Card {
            ArrowPreference(
                startAction = { StartActionColoredIcon(
                    color = AppTheme.colorfulPaletteColors.f,
                    imageVector = Syncthing,
                    vectorModifier = Modifier.scale(1.4f),
                ) },
                title = stringResource(Res.string.setting_core_selection),
                onClick = { onNavigateTo(AppSubPage.SETTINGS_CORE_MANAGE) },
            )

            ArrowPreference(
                startAction = { StartActionColoredIcon(
                    color = AppTheme.colorfulPaletteColors.e,
                    imageVector = MiuixIcons.Backup
                ) },
                title = stringResource(Res.string.setting_backup_settings),
                onClick = { onNavigateTo(AppSubPage.SETTINGS_BACKUP) },
            )
            if (developerModeVisible) {
                ArrowPreference(
                    startAction = { StartActionColoredIcon(
                        color = AppTheme.colorfulPaletteColors.a,
                        imageVector = MiuixIcons.Info
                    ) },
                    title = stringResource(Res.string.setting_developer_options),
                    onClick = { onNavigateTo(AppSubPage.DEV) },
                )
            }
        }
    }
}

@Composable
internal fun SettingCommonScreen(
    uiState: SettingUiState,
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val fullSettingEnabled = uiState.settingRaw != null && uiState.accessMode != null &&
        !uiState.isSaving && uiState.accessMode != SettingAccessMode.STARTUP_ONLY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal)
    ) {
        InfoSwitchCard(title = stringResource(Res.string.setting_general)) {
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.common_device_name),
                valueLabel = stringResource(Res.string.common_required),
                originalValue = uiState.formState.deviceName,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.DEVICE_NAME, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.DEVICE_NAME, value)
                },
                enabled = fullSettingEnabled,
            )
            InfoSwitch(
                title = stringResource(Res.string.setting_usage_reporting),
                summary = stringResource(Res.string.setting_usage_reporting_summary),
                checked = uiState.formState.usageReportingEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(usageReportingEnabled = it) },
            )
        }
    }
}

@Composable
internal fun SettingStorageScreen(
    uiState: SettingUiState,
    settingViewModel: SettingViewModel,
    onEditingStoragePermission: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val fullSettingEnabled = uiState.settingRaw != null && uiState.accessMode != null &&
        !uiState.isSaving && uiState.accessMode != SettingAccessMode.STARTUP_ONLY

    var showEditFreeSpaceOverlay by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal)
    ) {
        InfoSwitchCard(title = stringResource(Res.string.setting_general)) {
            ArrowPreference(
                title = stringResource(Res.string.setting_minimum_free_disk_space),
                summary = uiState.formState.minHomeDiskFree.ifBlank { "1" } + uiState.formState.minHomeDiskFreeUnit.displayName,
                onClick = { showEditFreeSpaceOverlay = true }
            )
            ArrowPreference(
                title = stringResource(Res.string.setting_page_storage_permission),
                onClick = onEditingStoragePermission,
            )
        }
    }

    WindowDialog(
        title = stringResource(Res.string.setting_minimum_free_disk_space),
        show = showEditFreeSpaceOverlay,
        onDismissRequest = { showEditFreeSpaceOverlay = false },
        onDismissFinished = { showEditFreeSpaceOverlay = false },
    ) {
        var value by rememberSaveable { mutableStateOf(uiState.formState.minHomeDiskFree) }
        var selectedIndex by rememberSaveable { mutableStateOf(uiState.formState.minHomeDiskFreeUnit.ordinal) }
        val selectedUnit = SettingConfiguration.DiskSpaceUnit.entries[selectedIndex]
        val validation = validateSettingEdit(uiState, SettingEditField.DISK_SPACE, value, selectedUnit)

        Column ( verticalArrangement = Arrangement.spacedBy(10.dp) ) {
            TextWithOptionField(
                value = value,
                onValueChange = { value = it },
                label = "1",
                useLabelAsPlaceholder = true,
                singleLine = true,
                items = SettingConfiguration.DiskSpaceUnit.entries.map { it.displayName },
                selectedIndex = selectedIndex,
                enabled = fullSettingEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                onSelectedIndexChange = { index ->
                    selectedIndex = index
                },
            )
            SettingEditError(validation.error)
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_cancel),
                    onClick = { showEditFreeSpaceOverlay = false }
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_confirm),
                    enabled = validation.canSubmit,
                    onClick = {
                        if (settingViewModel.submitSettingEdit(SettingEditField.DISK_SPACE, value, selectedUnit)) {
                            showEditFreeSpaceOverlay = false
                        }
                    },
                    colors = textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
internal fun SettingWebuiScreen(
    uiState: SettingUiState,
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val settingAvailable = uiState.settingRaw != null && uiState.accessMode != null
    val settingEditable = settingAvailable && !uiState.isSaving
    val fullSettingEnabled = settingEditable && uiState.accessMode != SettingAccessMode.STARTUP_ONLY

    val setting = uiState.settingRaw ?: SettingConfiguration.startupDefaults(
        guiListenAddress = settingViewModel.addressProtocolStack.guiListenAddress,
    )

    val openCertificatePicker = rememberPemFilePicker { result ->
        when (result) {
            is FilePickerResult.Selected -> settingViewModel.stageGuiTlsFile(
                GuiTlsFile.CERTIFICATE,
                result.content,
            )
            is FilePickerResult.Error -> settingViewModel.reportError(result.message)
            FilePickerResult.Cancelled -> Unit
        }
    }
    val openPrivateKeyPicker = rememberPemFilePicker { result ->
        when (result) {
            is FilePickerResult.Selected -> settingViewModel.stageGuiTlsFile(
                GuiTlsFile.PRIVATE_KEY,
                result.content,
            )
            is FilePickerResult.Error -> settingViewModel.reportError(result.message)
            FilePickerResult.Cancelled -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoSwitchCard(title = stringResource(Res.string.setting_general)) {
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.setting_port),
                valueLabel = "8384",
                originalValue = uiState.formState.guiPort,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.GUI_PORT, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.GUI_PORT, value)
                },
                enabled = settingAvailable,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            WindowDropdownPreference(
                title = stringResource(Res.string.setting_port_increment),
                items = SettingConfiguration.GuiPortConflictBehavior.entries.map { it.localizedDisplayName() },
                selectedIndex = uiState.formState.guiPortConflictBehavior.ordinal,
                enabled = settingEditable,
                onSelectedIndexChange = { index ->
                    settingViewModel.onFormChange(
                        guiPortConflictBehavior = SettingConfiguration.GuiPortConflictBehavior.entries[index],
                    )
                },
            )

            InfoSwitch(
                title = stringResource(Res.string.setting_authentication),
                summary = stringResource(Res.string.setting_authentication_summary),
                checked = uiState.formState.guiAuthenticationEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = {
                    settingViewModel.onFormChange(guiAuthenticationEnabled = it)
                },
            )

            AnimatedVisibility(
                visible = !settingAvailable || uiState.formState.guiAuthenticationEnabled,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
            ) {
                Column {
                    EditSettingItemWindowPopupArrow(
                        title = stringResource(Res.string.setting_authentication_user),
                        valueLabel = stringResource(Res.string.common_required),
                        originalValue = uiState.formState.guiUser,
                        validation = { value ->
                            validateSettingEdit(uiState, SettingEditField.GUI_USER, value)
                        },
                        onSubmit = { value ->
                            settingViewModel.submitSettingEdit(SettingEditField.GUI_USER, value)
                        },
                        enabled = fullSettingEnabled,
                    )
                    EditSettingItemWindowPopupArrow(
                        title = stringResource(Res.string.setting_authentication_password),
                        valueLabel = if (setting.guiPasswordConfigured) "···" else stringResource(Res.string.common_required),
                        summary = if (setting.guiPasswordConfigured) "***" else stringResource(Res.string.common_required),
                        originalValue = "",
                        validation = { value ->
                            validateSettingEdit(uiState, SettingEditField.GUI_PASSWORD, value)
                        },
                        onSubmit = { value ->
                            settingViewModel.submitSettingEdit(SettingEditField.GUI_PASSWORD, value)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = fullSettingEnabled,
                    )
                }
            }
        }

        InfoSwitchCard(stringResource(Res.string.setting_advanced)) {
            Column {
                WindowDropdownPreference(
                    title = stringResource(Res.string.setting_webui_theme),
                    items = SettingConfiguration.GuiTheme.entries.map { it.localizedDisplayName() },
                    selectedIndex = uiState.formState.guiTheme.ordinal,
                    enabled = fullSettingEnabled,
                    onSelectedIndexChange = { index ->
                        settingViewModel.onFormChange(
                            guiTheme = SettingConfiguration.GuiTheme.entries[index],
                        )
                    },
                )
                InfoSwitch(
                    title = stringResource(Res.string.setting_use_https_webui),
                    checked = uiState.formState.guiUseTls,
                    enabled = settingEditable,
                    onCheckedChange = { settingViewModel.onFormChange(guiUseTls = it) },
                )
                AnimatedVisibility(
                    visible = uiState.formState.guiUseTls,
                ) {
                    Column {
                        ArrowPreference(
                            title = stringResource(Res.string.setting_import_https_certificate),
                            enabled = settingEditable,
                            onClick = openCertificatePicker,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.setting_import_https_private_key),
                            enabled = settingEditable,
                            onClick = openPrivateKeyPicker,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingConnectionScreen(
    uiState: SettingUiState,
    settingViewModel: SettingViewModel,
    onEditingDiscoverServers: () -> Unit,
    onEditingListenAddresses: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val settingAvailable = uiState.settingRaw != null && uiState.accessMode != null
    val settingEditable = settingAvailable && !uiState.isSaving
    val fullSettingEnabled = settingEditable && uiState.accessMode != SettingAccessMode.STARTUP_ONLY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoSwitchCard(title = stringResource(Res.string.common_connection)) {
            ArrowPreference(
                title = stringResource(Res.string.common_label_listen_addresses),
                onClick = onEditingListenAddresses,
                enabled = fullSettingEnabled,
            )
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.common_upload_limit_kib),
                valueLabel = stringResource(Res.string.common_unlimited),
                originalValue = uiState.formState.maxSendKiBPerSecond,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.UPLOAD_LIMIT, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.UPLOAD_LIMIT, value)
                },
                enabled = fullSettingEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.common_download_limit_kib),
                valueLabel = stringResource(Res.string.common_unlimited),
                originalValue = uiState.formState.maxReceiveKiBPerSecond,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.DOWNLOAD_LIMIT, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.DOWNLOAD_LIMIT, value)
                },
                enabled = fullSettingEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.setting_reconnect_interval_seconds),
                valueLabel = "60",
                originalValue = uiState.formState.reconnectionIntervalSeconds,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.RECONNECT_INTERVAL, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.RECONNECT_INTERVAL, value)
                },
                enabled = fullSettingEnabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            InfoSwitch(
                title = stringResource(Res.string.setting_lan_rate_limit),
                summary = stringResource(Res.string.setting_lan_rate_limit_summary),
                checked = uiState.formState.limitBandwidthInLan,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(limitBandwidthInLan = it) },
            )
        }

        InfoSwitchCard(title = stringResource(Res.string.common_label_device_discovery)) {
            InfoSwitch(
                title = stringResource(Res.string.setting_global_discovery),
                summary = stringResource(Res.string.setting_global_discovery_summary),
                checked = uiState.formState.globalDiscoveryEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(globalDiscoveryEnabled = it) },
            )

            AnimatedVisibility(
                visible = !settingAvailable || uiState.formState.globalDiscoveryEnabled,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
            ) {
                Column {
                    InfoSwitch(
                        title = stringResource(Res.string.setting_announce_lan_addresses),
                        summary = stringResource(Res.string.setting_announce_lan_addresses_summary),
                        checked = uiState.formState.announceLanAddresses,
                        enabled = fullSettingEnabled,
                        onCheckedChange = { settingViewModel.onFormChange(announceLanAddresses = it) },
                    )

                    ArrowPreference(
                        title = stringResource(Res.string.setting_global_discovery_servers),
                        onClick = onEditingDiscoverServers,
                        enabled = fullSettingEnabled,
                    )
                }
            }

            InfoSwitch(
                title = stringResource(Res.string.setting_local_discovery),
                summary = stringResource(Res.string.setting_local_discovery_summary),
                checked = uiState.formState.localDiscoveryEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(localDiscoveryEnabled = it) },
            )

            AnimatedVisibility(
                visible = !settingAvailable || uiState.formState.localDiscoveryEnabled,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
            ) {
                Column {
                    EditSettingItemWindowPopupArrow(
                        title = stringResource(Res.string.setting_ipv4_multicast_port),
                        valueLabel = "21027",
                        originalValue = uiState.formState.localDiscoveryPort,
                        validation = { value ->
                            validateSettingEdit(uiState, SettingEditField.DISCOVERY_PORT, value)
                        },
                        onSubmit = { value ->
                            settingViewModel.submitSettingEdit(SettingEditField.DISCOVERY_PORT, value)
                        },
                        enabled = fullSettingEnabled,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    EditSettingItemWindowPopupArrow(
                        title = stringResource(Res.string.setting_ipv6_multicast_address),
                        valueLabel = "[ff12::8384]:21027",
                        originalValue = uiState.formState.localDiscoveryMulticastAddress,
                        validation = { value ->
                            validateSettingEdit(uiState, SettingEditField.DISCOVERY_ADDRESS, value)
                        },
                        onSubmit = { value ->
                            settingViewModel.submitSettingEdit(SettingEditField.DISCOVERY_ADDRESS, value)
                        },
                        enabled = fullSettingEnabled,
                    )
                }
            }
        }

        InfoSwitchCard(title = stringResource(Res.string.setting_network)) {
            OverlayDropdownPreference(
                title = stringResource(Res.string.setting_protocol_stack),
                summary = stringResource(Res.string.setting_connection_protocol_stack),
                items = SettingProtocolStack.entries.map { it.localizedDisplayName() },
                selectedIndex = settingViewModel.addressProtocolStack.ordinal,
                enabled = settingEditable,
                onSelectedIndexChange = { index ->
                    settingViewModel.updateAddressProtocolStack(SettingProtocolStack.entries[index])
                },
                onExpandedChange = {},
            )
            InfoSwitch(
                title = stringResource(Res.string.setting_nat_traversal),
                summary = stringResource(Res.string.setting_nat_traversal_summary),
                checked = uiState.formState.natEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(natEnabled = it) },
            )
            InfoSwitch(
                title = stringResource(Res.string.setting_use_relays),
                summary = stringResource(Res.string.setting_use_relays_summary),
                checked = uiState.formState.relaysEnabled,
                enabled = fullSettingEnabled,
                onCheckedChange = { settingViewModel.onFormChange(relaysEnabled = it) },
            )
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.setting_additional_lan_subnets),
                valueLabel = stringResource(Res.string.setting_cidr_one_per_line),
                originalValue = uiState.formState.alwaysLocalNetworks,
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.LAN_SUBNETS, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.LAN_SUBNETS, value)
                },
                enabled = fullSettingEnabled,
            )
            EditSettingItemWindowPopupArrow(
                title = stringResource(Res.string.setting_maximum_connections),
                valueLabel = stringResource(Res.string.common_unlimited),
                originalValue = uiState.formState.connectionLimitMax,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                validation = { value ->
                    validateSettingEdit(uiState, SettingEditField.MAX_CONNECTIONS, value)
                },
                onSubmit = { value ->
                    settingViewModel.submitSettingEdit(SettingEditField.MAX_CONNECTIONS, value)
                },
                enabled = fullSettingEnabled,
            )
        }
    }
}

@Composable
internal fun SettingAutoStartScreen(
    settingViewModel: SettingViewModel,
    onNavigateTo: (AppSubPage) -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card {
            OverlayDropdownPreference(
                title = stringResource(Res.string.setting_auto_start),
                items = AutoStartModeType.entries.map { it.localizedDisplayName() },
                selectedIndex = settingViewModel.autoStartMode.ordinal,
                enabled = true,
                onSelectedIndexChange = { index ->
                    settingViewModel.updateAutoStartMode(AutoStartModeType.entries[index])
                },
            )

            AnimatedVisibility(
                visible = settingViewModel.autoStartMode == AutoStartModeType.WITH_CONDITION,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 300)
                ),
            ) {
                Column {
                    ArrowPreference(
                        title = stringResource(Res.string.setting_page_network_conditions),
                        onClick = { onNavigateTo(AppSubPage.SETTINGS_BACKGROUND_RUNNING_NETWORK) },
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.setting_page_battery_conditions),
                        onClick = { onNavigateTo(AppSubPage.SETTINGS_BACKGROUND_RUNNING_BATTERY) },
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.setting_page_time_ranges),
                        onClick = { onNavigateTo(AppSubPage.SETTINGS_BACKGROUND_RUNNING_DURATION) },
                    )
                    ArrowPreference(
                        stringResource(Res.string.common_advanced),
                        onClick = { onNavigateTo(AppSubPage.SETTINGS_BACKGROUND_RUNNING_ADVANCED) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingEditListenScreen(
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()

    val uiState by settingViewModel.uiState.collectAsState()
    val settingEnabled = uiState.settingRaw != null && !uiState.isLoading && !uiState.isSaving
    val isSettingProtocolStackCustom = settingViewModel.addressProtocolStack == SettingProtocolStack.CUSTOM

    var currentRelay by remember { mutableStateOf(settingViewModel.listenAddressSettingUnsaved.relays.toList()) }
    var currentProtocolStack by remember { mutableStateOf(settingViewModel.actualListenStack.ordinal) }
    var currentTCP by remember { mutableStateOf(settingViewModel.listenAddressSettingUnsaved.tcp) }
    var currentQUIC by remember { mutableStateOf(settingViewModel.listenAddressSettingUnsaved.quic) }

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = stringResource(Res.string.setting_page_listen_addresses),
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton( onClick = navigateBack ) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(Res.string.common_action_cancel),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        settingViewModel.listenAddressSettingUnsaved = settingViewModel.listenAddressSettingUnsaved.copy(
                            tcp = currentTCP,
                            quic = currentQUIC,
                            relays = currentRelay.filter { it.uri.isNotBlank() && it.uri != "relay://" },
                            stackPrefer = UriProtocolStack.entries.getOrNull(currentProtocolStack)!!
                        )
                        //TODO: return when error occur
                        navigateBack()
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Ok,
                        contentDescription = stringResource(Res.string.common_action_confirm),
                    )
                }
            }
        ) },
    ) { padding ->
        Box (
            modifier = Modifier
                .barBackdropSource(barBackdrop)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = pagePaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card {
                    OverlayDropdownPreference(
                        title = stringResource(Res.string.setting_protocol_stack),
                        summary = stringResource(Res.string.setting_listen_protocol_stack),
                        items = UriProtocolStack.entries.map { it.localizedDisplayName() },
                        selectedIndex = currentProtocolStack,
                        enabled = settingEnabled && isSettingProtocolStackCustom,
                        onSelectedIndexChange = { index ->
                            currentProtocolStack = index
                        },
                    )
                    InfoSwitch(
                        title = "TCP",
                        checked = currentTCP,
                        enabled = settingEnabled,
                        onCheckedChange = { value -> currentTCP = value },
                    )
                    InfoSwitch(
                        title = "QUIC",
                        checked = currentQUIC,
                        enabled = settingEnabled,
                        onCheckedChange = { value -> currentQUIC = value },
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, start = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.setting_relay_servers))
                        IconButton(
                            enabled = settingEnabled && (
                                        currentRelay.lastOrNull()?.uri?.isNotBlank() ?: true
                                    ) && (
                                        currentRelay.lastOrNull()?.uri != "relay://"
                                    ),
                            onClick = { currentRelay += ListenAddressListItem(false,"relay://") },
                            content = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = stringResource(Res.string.setting_add_relay_server),
                                    imageVector = MiuixIcons.Add,
                                    tint = if (
                                        settingEnabled && (
                                            currentRelay.lastOrNull()?.uri?.isNotBlank() ?: true
                                        ) && (
                                            currentRelay.lastOrNull()?.uri != "relay://"
                                        )
                                    ) AppTheme.colorScheme.onSurface else AppTheme.colorScheme.disabledOnSurface,
                                )
                            },
                        )
                    }

                    Card {
                        Column {
                            currentRelay.forEachIndexed { index, item ->
                                CheckableInputValueRow(
                                    enabled = settingEnabled,
                                    state = item.enabled,
                                    value = item.uri,
                                    valueLabel = stringResource(Res.string.common_required),
                                    readOnly = item.uri == "default",
                                    onValueChange = { result ->
                                        currentRelay = currentRelay.mapIndexed { itemIndex, item2 ->
                                            if (itemIndex == index) {
                                                item2.copy(uri = result)
                                            } else item2
                                        }
                                    },
                                    onStateChange = {
                                        currentRelay = currentRelay.mapIndexed { itemIndex, item2 ->
                                            if (itemIndex == index) {
                                                item2.copy(enabled = !item2.enabled)
                                            } else item2
                                        }
                                    },
                                    onDelete = { currentRelay = currentRelay.filterIndexed { itemIndex, _ -> itemIndex != index } },
                                    valueValidator = settingViewModel::listenRelayAddressValidator,
                                    content = if (item.uri == "default") {
                                        { Text(stringResource(Res.string.setting_default_relay_summary)) }
                                    } else {
                                        null
                                    },
                                    showDivider = index < currentRelay.count() - 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingEditDiscoveryScreen(
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
    navigateBack: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()

    val uiState by settingViewModel.uiState.collectAsState()
    val settingEnabled = uiState.settingRaw != null && !uiState.isLoading && !uiState.isSaving

    var currentAddress by remember {
        mutableStateOf(
            settingViewModel.discoveryAddressSettingUnsaved.toList()
        )
    }

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = stringResource(Res.string.setting_page_discovery_servers),
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton( onClick = navigateBack ) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(Res.string.common_action_cancel),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        settingViewModel.discoveryAddressSettingUnsaved = currentAddress.filter { it.uri.isNotBlank() }
                        //TODO: return when error occur
                        navigateBack()
                    }
                ) {
                    Icon(
                        imageVector = MiuixIcons.Ok,
                        contentDescription = stringResource(Res.string.common_action_confirm),
                    )
                }
            }
        ) },
    ) { padding ->
        Box (
            modifier = Modifier
                .barBackdropSource(barBackdrop)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = pagePaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card {
                    Text(
                        stringResource(Res.string.setting_discovery_servers_description),
                        style = AppTheme.textStyles.paragraph,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.setting_discovery_servers))
                        IconButton(
                            enabled = settingEnabled && currentAddress.lastOrNull()?.uri?.isNotBlank() ?: true,
                            onClick = { currentAddress += ListenAddressListItem(false, "") },
                            content = {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    contentDescription = stringResource(Res.string.setting_add_discovery_server),
                                    imageVector = MiuixIcons.Add,
                                    tint = if (
                                        settingEnabled && currentAddress.lastOrNull()?.uri?.isNotBlank() ?: true
                                    ) AppTheme.colorScheme.onSurface else AppTheme.colorScheme.disabledOnSurface,
                                )
                            },
                        )
                    }

                    Card {
                        Column {
                            currentAddress.forEachIndexed { index, item ->
                                val pingState = settingViewModel.discoveryServerPingState(item.uri)
                                CheckableInputValueRow(
                                    enabled = settingEnabled,
                                    state = item.enabled,
                                    value = item.uri,
                                    valueLabel = stringResource(Res.string.common_required),
                                    singleLine = true,
                                    onValueChange = { result ->
                                        settingViewModel.clearDiscoveryServerPingState(item.uri)
                                        currentAddress =
                                            currentAddress.mapIndexed { itemIndex, currentItem ->
                                                if (itemIndex == index) currentItem.copy(uri = result) else currentItem
                                            }
                                    },
                                    onStateChange = {
                                        currentAddress =
                                            currentAddress.mapIndexed { itemIndex, currentItem ->
                                                if (itemIndex == index) {
                                                    currentItem.copy(enabled = !item.enabled)
                                                } else {
                                                    currentItem
                                                }
                                            }
                                    },
                                    onDelete = {
                                        settingViewModel.clearDiscoveryServerPingState(item.uri)
                                        currentAddress =
                                            currentAddress.filterIndexed { itemIndex, _ ->
                                                itemIndex != index
                                            }
                                    },
                                    showDivider = index < currentAddress.count() - 1
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                enabled = pingState != DiscoveryServerPingState.InProgress &&
                                                        item.uri.isNotBlank() && item.uri != "default",
                                                onClick = {
                                                    settingViewModel.pingDiscoveryServer(
                                                        item.uri
                                                    )
                                                },
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            stringResource(Res.string.setting_latency),
                                            style = AppTheme.textStyles.body2,
                                            color = AppTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                        Text(
                                            when (pingState) {
                                                DiscoveryServerPingState.InProgress -> "···"
                                                is DiscoveryServerPingState.Success -> "${pingState.latencyMillis} ms"
                                                is DiscoveryServerPingState.Failure -> stringResource(
                                                    Res.string.common_failed
                                                )

                                                null -> "—"
                                            },
                                            style = AppTheme.textStyles.body2,
                                            color = when (pingState) {
                                                is DiscoveryServerPingState.Success -> {
                                                    if (pingState.latencyMillis < 100) AppTheme.statusColors.ok else AppTheme.statusColors.pending
                                                }

                                                is DiscoveryServerPingState.Failure -> AppTheme.colorScheme.error
                                                else -> AppTheme.colorScheme.onSurfaceVariantSummary
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingStoragePermissionPage(
    granted: Boolean,
    onRequestPermission: () -> Unit,
    navigateBack: () -> Unit,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
    folderId: String? = null,
    selectedFolderPath: String? = null,
    onFolderPathSelected: ((String?) -> Unit)? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = MiuixScrollBehavior()
    val defaultPath = folderId?.let(::defaultFolderPath)
    var chosenFolderPath by remember(folderId, selectedFolderPath) {
        mutableStateOf(selectedFolderPath?.takeUnless { it == defaultPath })
    }
    var selectedFolderType by remember(folderId, selectedFolderPath) {
        mutableIntStateOf(
            if (selectedFolderPath == null || selectedFolderPath == defaultPath) 0 else 1,
        )
    }
    var folderPickerError by remember { mutableStateOf<String?>(null) }
    val openFolderPicker = rememberFolderPicker { result ->
        when (result) {
            FolderPickerResult.Cancelled -> Unit
            is FolderPickerResult.Error -> folderPickerError = result.message
            is FolderPickerResult.Selected -> {
                chosenFolderPath = result.path
                selectedFolderType = 1
                folderPickerError = null
                onFolderPathSelected?.invoke(result.path)
            }
        }
    }

    LaunchedEffect(folderPickerError) {
        folderPickerError?.let { snackbarHostState.showSnackbar(it) }
        folderPickerError = null
    }

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = { BlurredSmallTopAppBar(
            title = stringResource(Res.string.setting_page_storage_permission),
            scrollBehavior = scrollBehavior,
            backdrop = barBackdrop,
            navigationIcon = {
                IconButton( onClick = navigateBack ) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = stringResource(Res.string.common_action_back),
                    )
                }
            },
        ) },
        snackbarHost = {
            SnackbarHost(state = snackbarHostState)
        },
    ) { padding ->
        Box (
            modifier = Modifier
                .barBackdropSource(barBackdrop)
                .nestedScroll(
                    scrollBehavior.nestedScrollConnection,
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = pagePaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!granted || (folderId == null)) {
                    MessageCard(
                        title = stringResource(Res.string.setting_public_storage_access),
                        message = stringResource(Res.string.setting_public_storage_access_message),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        InfoSwitch(
                            title = stringResource(Res.string.setting_public_storage_access),
                            checked = granted,
                            onCheckedChange = { onRequestPermission() },
                            enabled = true,
                        )
                    }
                }

                if (folderId != null && onFolderPathSelected != null) {
                    InfoSwitchCard(
                        title = stringResource(Res.string.setting_select_path)
                    ) {
                        RadioButtonPreference(
                            title = stringResource(Res.string.setting_default_path),
                            summary = defaultPath,
                            selected = selectedFolderType == 0,
                            onClick = {
                                selectedFolderType = 0
                                chosenFolderPath = null
                                onFolderPathSelected.invoke(null)
                            },
                        )

                        RadioButtonPreference(
                            title = stringResource(Res.string.setting_external_path),
                            enabled = granted,
                            summary = chosenFolderPath,
                            selected = selectedFolderType == 1,
                            onClick = openFolderPicker,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingCoreSelectScreen(
    uiState: CoreUiState,
    snackbarHostState: SnackbarHostState,
    onCoreSelected: (String) -> Unit,
    onImportCore: () -> Unit,
    onCoreDelete: (String) -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, start = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(Res.string.setting_syncthing_core))
            IconButton(
                onClick = onImportCore,
                enabled = uiState.canImportCore,
                content = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        contentDescription = stringResource(Res.string.setting_import_core),
                        imageVector = MiuixIcons.Add,
                    )
                },
            )
        }

        if (!uiState.availableCores.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(
                        items = uiState.availableCores,
                        key = { _, option -> option.id },
                    ) { _, option ->
                        val available = option.availability == CoreAvailability.AVAILABLE
                        val selected = option.id == uiState.selectedCoreId
                        CheckableInputValueRow(
                            state = selected,
                            value = if (option.internal) {
                                "${stringResource(Res.string.setting_internal_core)} v${option.version}" +
                                    (option.unavailableReason ?: "")
                            } else {
                                option.version + (option.unavailableReason ?: "")
                            },
                            onValueChange = {},
                            valueValidator = { available },
                            onStateChange = { if (!selected && available) onCoreSelected(option.id) },
                            enabled = uiState.canSelectCore,
                            readOnly = true,
                            onDelete = if (option.internal || selected) null else {{onCoreDelete(option.id)}},
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingBackgroundRunningPage(
    batteryOptimizationExempt: Boolean,
    onBatteryOptimizationRequest: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    navigateBack: () -> Unit,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
) {
    var selectedTabIndex by remember { mutableStateOf(BackgroundRunningSystemType.ANDROID) }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = {
            BlurredSmallTopAppBar(
                title = stringResource(Res.string.setting_page_background_running),
                scrollBehavior = scrollBehavior,
                backdrop = barBackdrop,
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(Res.string.common_action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .barBackdropSource(barBackdrop)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = pagePaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            var showBackgroundLockOverlay by rememberSaveable { mutableStateOf(false) }

            TabRow(
                tabs = listOf("Android", stringResource(Res.string.setting_xiaomi)),
                selectedTabIndex = selectedTabIndex.ordinal,
                onTabSelected = { index ->
                    selectedTabIndex = BackgroundRunningSystemType.entries[index]
                },
                modifier = Modifier.padding(vertical = pagePaddingHorizontal)
            )

            when (selectedTabIndex) {
                BackgroundRunningSystemType.ANDROID -> {
                    MessageCard(
                        title = stringResource(Res.string.setting_ignore_battery_optimization),
                        message = stringResource(Res.string.setting_battery_optimization_message),
                    ) {
                        InfoSwitch(
                            title = stringResource(Res.string.setting_allow_ignore_battery_optimization),
                            checked = batteryOptimizationExempt,
                            onCheckedChange = { onBatteryOptimizationRequest() },
                            enabled = true,
                        )
                    }
                }

                BackgroundRunningSystemType.XIAOMI -> {
                    MessageCard(
                        title = stringResource(Res.string.setting_ignore_battery_optimization),
                        message = stringResource(Res.string.setting_background_permissions_message),
                    ) {
                        InfoSwitch(
                            title = stringResource(Res.string.setting_allow_ignore_battery_optimization),
                            checked = batteryOptimizationExempt,
                            onCheckedChange = { onBatteryOptimizationRequest() },
                            enabled = true,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.setting_allow_app_auto_start),
                            onClick = onOpenAppDetailsSettings,
                        )
                        ArrowPreference(
                            title = stringResource(Res.string.setting_lock_background_process),
                            onClick = { showBackgroundLockOverlay = true },
                        )
                    }
                }
            }

            OverlayDialog(
                title = stringResource(Res.string.setting_lock_background_process),
                show = showBackgroundLockOverlay,
                onDismissRequest = { showBackgroundLockOverlay = false },
                onDismissFinished = { showBackgroundLockOverlay = false },
            ) {
                Column ( verticalArrangement = Arrangement.spacedBy(20.dp) ) {
                    Text(stringResource(Res.string.setting_xiaomi_lock_instructions))
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.common_action_confirm),
                        onClick = { showBackgroundLockOverlay = false },
                        colors = TextButtonColors(
                            color = AppTheme.colorScheme.primary,
                            textColor = AppTheme.colorScheme.onPrimary,
                            disabledColor = AppTheme.colorScheme.primary,
                            disabledTextColor = AppTheme.colorScheme.disabledOnPrimary,
                            borderColor = AppTheme.colorScheme.dividerLine,
                        )
                    )
                }
            }
        }
    }
}

private enum class BackgroundRunningSystemType {
    ANDROID,
    XIAOMI,
}

@Composable
internal fun SettingBackgroundRunningNetworkPage(
    settingViewModel: SettingViewModel,
    currentWifiName: String?,
    wifiNameAccessGranted: Boolean,
    locationServiceEnabled: Boolean,
    onRequestWifiNameAccess: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val autoStartCondition = settingViewModel.autoStartCondition
    val condition = autoStartCondition.network
    var wifiNamesText by rememberSaveable {
        mutableStateOf(condition.wifiNames.sorted().joinToString("\n"))
    }

    LaunchedEffect(condition.wifiNames) {
        val textNames = wifiNamesText.toWifiNames()
        if (textNames != condition.wifiNames) {
            wifiNamesText = condition.wifiNames.sorted().joinToString("\n")
        }
    }

    fun updateCondition(updated: NetworkRunCondition) {
        settingViewModel.updateAutoStartCondition(
            autoStartCondition.copy(network = updated),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!wifiNameAccessGranted) {
            MessageCard(
                title = stringResource(Res.string.setting_grant_location_permission),
                message = stringResource(Res.string.setting_location_permission_required),
            ) {
                InfoSwitch(
                    title = stringResource(Res.string.setting_authorize_location_permission),
                    checked = wifiNameAccessGranted,
                    onCheckedChange = { onRequestWifiNameAccess() },
                )
            }
        } else if (!locationServiceEnabled) {
            MessageCard(
                title = stringResource(Res.string.setting_enable_location_services),
                message = stringResource(Res.string.setting_location_services_required),
            ) {
                ArrowPreference(
                    title = stringResource(Res.string.setting_enable_location_services),
                    onClick = onOpenLocationSettings,
                )
            }
        }

        MessageCard(
            title = stringResource(Res.string.setting_follow_network_conditions),
            message = stringResource(Res.string.setting_follow_network_conditions_summary),
        ) {
            InfoSwitch(
                title = stringResource(Res.string.setting_follow_network_conditions),
                checked = condition.enabled,
                onCheckedChange = {
                    updateCondition(condition.copy(enabled = it))
                },
            )
        }

        AnimatedVisibility(
            visible = condition.enabled,
            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
        ) {
            Column {
                InfoSwitchCard(title = "WLAN") {
                    InfoSwitch(
                        title = stringResource(Res.string.setting_run_on_wlan),
                        checked = condition.runOnWifi,
                        enabled = true,
                        onCheckedChange = { updateCondition(condition.copy(runOnWifi = it)) },
                    )
                    AnimatedVisibility(
                        visible = condition.runOnWifi,
                        enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
                    ) {
                        Column {
                            InfoSwitch(
                                title = stringResource(Res.string.setting_run_on_metered_wlan),
                                checked = condition.runOnMeteredWifi,
                                enabled = true,
                                onCheckedChange = {
                                    updateCondition(
                                        condition.copy(
                                            runOnMeteredWifi = it,
                                            restrictWifiNames = false
                                        )
                                    )
                                },
                            )
                            InfoSwitch(
                                title = stringResource(Res.string.setting_run_on_selected_wlan),
                                checked = condition.restrictWifiNames,
                                enabled = true,
                                onCheckedChange = {
                                    updateCondition(
                                        condition.copy(
                                            restrictWifiNames = it,
                                            runOnMeteredWifi = false
                                        )
                                    )
                                },
                            )
                            AnimatedVisibility(
                                visible = condition.restrictWifiNames,
                                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
                            ) {
                                Column {
                                    ArrowPreference(
                                        title = stringResource(Res.string.setting_add_current_wlan),
                                        summary = currentWifiName
                                            ?: stringResource(Res.string.setting_wlan_unavailable),
                                        enabled = currentWifiName != null,
                                        onClick = {
                                            currentWifiName?.let { wifiName ->
                                                val updatedNames = condition.wifiNames + wifiName
                                                wifiNamesText = updatedNames.sorted().joinToString("\n")
                                                updateCondition(condition.copy(wifiNames = updatedNames))
                                            }
                                        },
                                    )
                                    InputValueRow(
                                        value = wifiNamesText,
                                        onValueChange = { value ->
                                            wifiNamesText = value
                                            updateCondition(condition.copy(wifiNames = value.toWifiNames()))
                                        },
                                        label = stringResource(Res.string.setting_wlan_names),
                                        valueLabel = stringResource(Res.string.setting_one_per_line),
                                        singleLine = false,
                                    )
                                }
                            }
                        }
                    }
                }

                InfoSwitchCard(title = stringResource(Res.string.setting_mobile_data)) {
                    InfoSwitch(
                        title = stringResource(Res.string.setting_run_on_mobile_data),
                        checked = condition.runOnMobileData,
                        enabled = true,
                        onCheckedChange = { updateCondition(condition.copy(runOnMobileData = it)) },
                    )
                    AnimatedVisibility(
                        visible = condition.runOnMobileData,
                        enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
                    ) {
                        InfoSwitch(
                            title = stringResource(Res.string.setting_run_while_roaming),
                            checked = condition.runOnRoaming,
                            enabled = true,
                            onCheckedChange = { updateCondition(condition.copy(runOnRoaming = it)) },
                        )
                    }
                }

                InfoSwitchCard(title = stringResource(Res.string.common_advanced)) {
                    InfoSwitch(
                        title = stringResource(Res.string.setting_run_without_network),
                        summary = stringResource(Res.string.setting_airplane_mode_network_summary),
                        checked = condition.runWithoutNetwork,
                        enabled = true,
                        onCheckedChange = { updateCondition(condition.copy(runWithoutNetwork = it)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun SettingBackgroundRunningBatteryPage(
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val autoStartCondition = settingViewModel.autoStartCondition
    val condition = autoStartCondition.battery

    fun updateCondition(updated: BatteryRunCondition) {
        settingViewModel.updateAutoStartCondition(
            autoStartCondition.copy(battery = updated),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card {
            WindowDropdownPreference(
                title = stringResource(Res.string.setting_power_source),
                items = SettingConfiguration.RunningOnPoweredBy.entries.map { it.localizedDisplayName() },
                selectedIndex = condition.poweredBy.ordinal,
                enabled = true,
                onSelectedIndexChange = { index ->
                    updateCondition(
                        condition.copy(
                            poweredBy = SettingConfiguration.RunningOnPoweredBy.entries[index],
                        ),
                    )
                },
            )

            InfoSwitch(
                title = stringResource(Res.string.setting_follow_power_saver),
                summary = stringResource(Res.string.setting_follow_power_saver_summary),
                checked = condition.respectPowerSaveMode,
                enabled = true,
                onCheckedChange = {
                    updateCondition(condition.copy(respectPowerSaveMode = it))
                },
            )

            InfoSwitch(
                title = stringResource(Res.string.setting_run_in_battery_range),
                summary = stringResource(Res.string.setting_run_in_battery_range_summary),
                checked = condition.levelRangeEnabled,
                onCheckedChange = {
                    updateCondition(condition.copy(levelRangeEnabled = it))
                },
            )

            AnimatedVisibility(
                visible = condition.levelRangeEnabled,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
            ) {
                RangeSliderPreference(
                    value = condition.minimumPercent.toFloat()..condition.maximumPercent.toFloat(),
                    onValueChange = { range ->
                        updateCondition(
                            condition.copy(
                                minimumPercent = range.start.toInt(),
                                maximumPercent = range.endInclusive.toInt(),
                            ),
                        )
                    },
                    title = stringResource(Res.string.setting_battery_range),
                    valueText = "${condition.minimumPercent}% – ${condition.maximumPercent}%",
                    valueRange = 0f..100f,
                    showKeyPoints = true,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    keyPoints = listOf(0f, 20f, 40f, 60f, 80f, 100f),
                )
            }
        }
    }
}

@Composable
internal fun SettingBackgroundRunningDurationPage(
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val autoStartCondition = settingViewModel.autoStartCondition

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card ( Modifier.padding(bottom = 10.dp )) {
            InfoSwitch(
                title = stringResource(Res.string.setting_run_on_schedule),
                summary = stringResource(Res.string.setting_run_on_schedule_summary),
                checked = autoStartCondition.scheduleEnabled,
                enabled = true,
                onCheckedChange = { enabled ->
                    settingViewModel.updateAutoStartCondition(
                        autoStartCondition.copy(scheduleEnabled = enabled),
                    )
                },
            )
        }

        AnimatedVisibility(
            visible = autoStartCondition.scheduleEnabled,
            enter = expandVertically(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, start = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(Res.string.setting_run_time_ranges))
                    IconButton(
                        onClick = settingViewModel::addExecuteSchedule,
                        enabled = true,
                        content = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                contentDescription = stringResource(Res.string.setting_add_time_range),
                                imageVector = MiuixIcons.Add,
                            )
                        },
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    if (!autoStartCondition.schedules.isEmpty()) {
                       Column(modifier = Modifier.fillMaxSize()) {
                           autoStartCondition.schedules.forEach {
                               SettingBackgroundRunningDurationPickRow(
                                   schedule = it,
                                   onUpdate = settingViewModel::updateExecuteSchedule,
                                   onDelete = {
                                       settingViewModel.removeExecuteSchedule(it.id)
                                   },
                               )
                           }
                        }
                    } else {
                        Spacer( modifier = Modifier.height(40.dp) )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingBackgroundRunningDurationPickRow(
    schedule: ExecuteSchedule,
    onUpdate: (ExecuteSchedule) -> Unit,
    onDelete: () -> Unit,
) {
    var timePickerTarget by remember { mutableStateOf<TimePickerTarget?>(null) }
    var runMinutesText by rememberSaveable(schedule.id) {
        mutableStateOf(schedule.runMinutes.toString())
    }
    var pauseMinutesText by rememberSaveable(schedule.id) {
        mutableStateOf(schedule.pauseMinutes.toString())
    }
    val use24HourFormat = isSystem24HourFormat()

    Column ( horizontalAlignment = Alignment.CenterHorizontally ) {
        Row( verticalAlignment = Alignment.CenterVertically ) {
            Box( modifier = Modifier.weight(1f) ) {
                WindowDropdownPreference(
                    title = schedule.type.localizedDisplayName(),
                    summary = when (schedule.type) {
                        ExecuteScheduleType.INTERVAL -> stringResource(
                            Res.string.setting_interval_schedule_summary,
                            schedule.runMinutes,
                            schedule.pauseMinutes,
                        )
                        ExecuteScheduleType.TIME_RANGE ->
                            "${formatMinuteOfDay(schedule.startMinuteOfDay, use24HourFormat)} – " +
                                    formatMinuteOfDay(schedule.endMinuteOfDay, use24HourFormat)
                    },
                    items = standardExecuteScheduleTypes.map { it.localizedDisplayName() },
                    selectedIndex = standardExecuteScheduleTypes.indexOf(schedule.type)
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onUpdate(schedule.copy(type = standardExecuteScheduleTypes[index]))
                    },
                )
            }
            DeleteBox(
                onDelete = onDelete,
                modifier = Modifier.padding(10.dp),
            )
        }

        when (schedule.type) {
            ExecuteScheduleType.INTERVAL -> {
                Column {
                    InputValueRow(
                        label = stringResource(Res.string.setting_run_duration_minutes),
                        value = runMinutesText,
                        valueLabel = stringResource(Res.string.common_required),
                        valueValidator = { it.toIntOrNull()?.let { value -> value > 0 } == true },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            runMinutesText = value
                            value.toIntOrNull()?.takeIf { it > 0 }?.let { minutes ->
                                onUpdate(schedule.copy(runMinutes = minutes))
                            }
                        },
                    )
                    InputValueRow(
                        label = stringResource(Res.string.setting_pause_duration_minutes),
                        value = pauseMinutesText,
                        valueLabel = stringResource(Res.string.common_required),
                        valueValidator = { it.toIntOrNull()?.let { value -> value > 0 } == true },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            pauseMinutesText = value
                            value.toIntOrNull()?.takeIf { it > 0 }?.let { minutes ->
                                onUpdate(schedule.copy(pauseMinutes = minutes))
                            }
                        },
                    )
                }
            }

            ExecuteScheduleType.TIME_RANGE -> {
                var showWeekDay by remember { mutableStateOf( false ) }

                Column {
                    ArrowPreference(
                        title = stringResource(Res.string.setting_from_time),
                        summary = formatMinuteOfDay(schedule.startMinuteOfDay, use24HourFormat),
                        onClick = { timePickerTarget = TimePickerTarget.START },
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.setting_to_time),
                        summary = formatMinuteOfDay(schedule.endMinuteOfDay, use24HourFormat) ,
                        onClick = { timePickerTarget = TimePickerTarget.END },
                    )

                    ArrowPreference(
                        title = stringResource(Res.string.setting_every_week),
                        summary = schedule.weekDays.weekdaySummary(),
                        onClick = { showWeekDay = !showWeekDay },
                    )

                    AnimatedVisibility(
                        visible = showWeekDay,
                        enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)),
                    ) {
                        Column {
                            localizedWeekDayNames().forEachIndexed { index, name ->
                                val day = index + 1
                                CheckableRow(
                                    title = name,
                                    state = day in schedule.weekDays,
                                    onClick = {
                                        val updatedDays = if (day in schedule.weekDays) {
                                            schedule.weekDays - day
                                        } else {
                                            schedule.weekDays + day
                                        }
                                        onUpdate(schedule.copy(weekDays = updatedDays))
                                    },
                                )
                            }
                        }
                    }
                }
            }

        }

        HorizontalDivider( modifier = Modifier.fillMaxWidth(0.9f) )
    }

    val selectedTarget = timePickerTarget
    OverlayDialog(
        show = selectedTarget != null,
        title = stringResource(
            if (selectedTarget == TimePickerTarget.END) Res.string.setting_select_end_time else Res.string.setting_select_start_time,
        ),
        onDismissRequest = { timePickerTarget = null },
        onDismissFinished = { timePickerTarget = null },
    ) {
        val initialMinuteOfDay = if (selectedTarget == TimePickerTarget.END) {
            schedule.endMinuteOfDay
        } else {
            schedule.startMinuteOfDay
        }
        TimePicker(
            initialHour = initialMinuteOfDay / 60,
            initialMinute = initialMinuteOfDay % 60,
            use24h = use24HourFormat,
            onTimeChange = { hour, minute ->
                val minuteOfDay = hour * 60 + minute
                onUpdate(
                    if (selectedTarget == TimePickerTarget.END) {
                        schedule.copy(endMinuteOfDay = minuteOfDay)
                    } else {
                        schedule.copy(startMinuteOfDay = minuteOfDay)
                    },
                )
            },
        )
    }
}

@Composable
internal fun SettingBackgroundRunningAdvancedPage(
    settingViewModel: SettingViewModel,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    val condition = settingViewModel.autoStartCondition

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MessageCard(
            title = stringResource(Res.string.setting_cron_triggers),
            message = stringResource(Res.string.setting_cron_triggers_summary),
        )

        CronTriggerEditor(
            title = stringResource(Res.string.setting_start_triggers),
            triggers = condition.startCronTriggers,
            onAdd = settingViewModel::addStartCronTrigger,
            onUpdate = settingViewModel::updateStartCronTrigger,
            onDelete = settingViewModel::removeStartCronTrigger,
        )

        CronTriggerEditor(
            title = stringResource(Res.string.setting_stop_triggers),
            triggers = condition.stopCronTriggers,
            onAdd = settingViewModel::addStopCronTrigger,
            onUpdate = settingViewModel::updateStopCronTrigger,
            onDelete = settingViewModel::removeStopCronTrigger,
        )
    }
}

@Composable
private fun CronTriggerEditor(
    title: String,
    triggers: List<CronTrigger>,
    onAdd: () -> Unit,
    onUpdate: (CronTrigger) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title)
            IconButton(
                onClick = onAdd,
                content = {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        contentDescription = stringResource(Res.string.setting_add_named_item, title),
                        imageVector = MiuixIcons.Add,
                    )
                },
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            if (triggers.isEmpty()) {
                Spacer( modifier = Modifier.height(40.dp) )
            } else {
                Column ( horizontalAlignment = Alignment.CenterHorizontally ) {
                    triggers.forEachIndexed { index, trigger ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InputValueRow(
                                modifier = Modifier.weight(1f),
                                label = stringResource(Res.string.setting_expression),
                                value = trigger.expression,
                                valueLabel = "* * * * *",
                                valueValidator = ::isValidCronExpression,
                                onValueChange = { expression ->
                                    onUpdate(trigger.copy(expression = expression))
                                },
                            )
                            DeleteBox(
                                modifier = Modifier.padding(end = 10.dp),
                                onDelete = { onDelete(trigger.id) },
                            )
                        }
                        InfoSwitch(
                            title = stringResource(Res.string.setting_follow_conditions),
                            summary = stringResource(Res.string.setting_follow_conditions_summary),
                            checked = trigger.respectConditions,
                            onCheckedChange = { respectConditions ->
                                onUpdate(trigger.copy(respectConditions = respectConditions))
                            },
                        )
                        if (index != triggers.lastIndex) {
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingPermissionPage(
    onEditingBackgroundPermission: () -> Unit,
    onEditingStoragePermission: () -> Unit,
    onEditingPositionPermission: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    Card (
        modifier = Modifier
            .padding(padding)
            .padding( horizontal = pagePaddingHorizontal )
    ) {
        Column {
            ArrowPreference(
                title = stringResource(Res.string.setting_background_permission),
                onClick = onEditingBackgroundPermission,
            )

            ArrowPreference(
                title = stringResource(Res.string.setting_page_storage_permission),
                onClick = onEditingStoragePermission,
            )

            ArrowPreference(
                title = stringResource(Res.string.setting_page_location_permission),
                onClick = onEditingPositionPermission,
            )
        }
    }
}

@Composable
internal fun SettingPositionPermissionPage(
    wifiNameAccessGranted: Boolean,
    onRequestWifiNameAccess: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    Column (
        modifier = Modifier
            .padding(padding)
            .padding( horizontal = pagePaddingHorizontal ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MessageCard(
            title = stringResource(Res.string.setting_grant_location_permission),
            message = stringResource(Res.string.setting_location_permission_required),
        ) {
            InfoSwitch(
                title = stringResource(Res.string.setting_authorize_location_permission),
                checked = wifiNameAccessGranted,
                onCheckedChange = { onRequestWifiNameAccess() },
            )
        }

        MessageCard(
            title = stringResource(Res.string.setting_enable_location_services),
            message = stringResource(Res.string.setting_location_services_required),
        ) {
            ArrowPreference(
                title = stringResource(Res.string.setting_enable_location_services),
                onClick = onOpenLocationSettings,
            )
        }
    }
}

@Composable
internal fun SettingThemePage(
    uiState: MainUiState,
    onPageToggle: (AppPage) -> Unit,
    onDefaultPageChange: (AppPage) -> Unit,
    onFloatingBottomBarChange: (Boolean) -> Unit,
    onTopBarBlurChange: (Boolean) -> Unit,
    onBottomBarBlurChange: (Boolean) -> Unit,
    onHighContrastModeChange: (Boolean) -> Unit,
    navigateBack: () -> Unit,
    scrollBehavior: ScrollBehavior,
    pagePaddingHorizontal: Dp,
    barBackdrop: LayerBackdrop?,
) {
    val selectedPagesInOrder = AppPage.entries.filter(uiState.bottomBarPages::contains)
    val blurSupported = isBarBlurSupported()

    Scaffold(
        containerColor = AppTheme.colorScheme.surface,
        topBar = {
            BlurredSmallTopAppBar(
                title = stringResource(Res.string.setting_page_appearance),
                scrollBehavior = scrollBehavior,
                backdrop = barBackdrop.takeIf { uiState.topBarBlurEnabled },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(Res.string.common_action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .barBackdropSource(barBackdrop)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = pagePaddingHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (blurSupported) {
                InfoSwitchCard (
                    title = stringResource(Res.string.setting_top_bar_settings)
                ) {
                    InfoSwitch(
                        title = stringResource(Res.string.setting_top_bar_blur),
                        checked = uiState.topBarBlurEnabled,
                        onCheckedChange = onTopBarBlurChange,
                    )
                }
            }

            InfoSwitchCard (
                title = stringResource(Res.string.setting_bottom_bar_items)
            ) {
                AppPage.entries.forEach { page ->
                    val selected = page in uiState.bottomBarPages
                    CheckableRow(
                        title = page.localizedTitle(),
                        state = selected,
                        enabled = if (selected) {
                            when (page) {
                                AppPage.SETTINGS -> AppPage.CORE in uiState.bottomBarPages
                                AppPage.CORE -> AppPage.SETTINGS in uiState.bottomBarPages
                                else -> uiState.bottomBarPages.size > 1
                            }
                        } else {
                            uiState.canSelectMoreBottomBarPages
                        },
                        onClick = { onPageToggle(page) },
                    )
                }
            }

            InfoSwitchCard (
                title = stringResource(Res.string.setting_bottom_bar_settings),
            ) {
                Column {
                    OverlayDropdownPreference(
                        title = stringResource(Res.string.setting_default_page),
                        summary = stringResource(Res.string.setting_default_page_summary),
                        items = selectedPagesInOrder.map { it.localizedTitle() },
                        selectedIndex = selectedPagesInOrder.indexOf(
                            uiState.defaultBottomBarPage,
                        ),
                        onSelectedIndexChange = { index ->
                            selectedPagesInOrder.getOrNull(index)?.let(onDefaultPageChange)
                        },
                    )
                    InfoSwitch(
                        title = stringResource(Res.string.setting_floating_bottom_bar),
                        checked = uiState.floatingBottomBar,
                        onCheckedChange = onFloatingBottomBarChange,
                    )
                    if (blurSupported) {
                        InfoSwitch(
                            title = stringResource(Res.string.setting_bottom_bar_blur),
                            checked = uiState.bottomBarBlurEnabled,
                            onCheckedChange = onBottomBarBlurChange,
                        )
                    }
                    Box (modifier = Modifier.padding(top = 8.dp)) {
                        AppNavigationBar(
                            navbarColor = AppTheme.colorScheme.background,
                            entries = AppPage.entries,
                            visiblePages = uiState.bottomBarPages,
                            currentPage = uiState.defaultBottomBarPage,
                            floating = uiState.floatingBottomBar,
                            defaultWindowInsetsPadding = false,
                            backdrop = null,
                        )
                    }
                }
            }

            InfoSwitchCard (
                title = stringResource(Res.string.setting_accessibility),
            ) {
                InfoSwitch(
                    title = stringResource(Res.string.setting_high_contrast_mode),
                    checked = uiState.highContrastMode,
                    onCheckedChange = onHighContrastModeChange,
                )
            }
        }
    }
}

@Composable
internal fun SettingBackupPage(
    uiState: BackupUiState,
    snackbarHostState: SnackbarHostState,
    onExport: (password: String?) -> Unit,
    onImport: (BackupImportFormat) -> Unit,
    onConfirmImport: (password: String?) -> Unit,
    onCancelImport: () -> Unit,
    onMessageShown: () -> Unit,
    pagePaddingHorizontal: Dp,
    padding: PaddingValues,
) {
    var showEncryptedExportDialog by rememberSaveable { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf(TextFieldValue()) }
    var exportPasswordConfirmation by remember { mutableStateOf(TextFieldValue()) }
    var importPassword by remember(uiState.pendingImport?.sourceUri) { mutableStateOf("") }
    val exportPasswordConfirmationFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val snackbarScope = rememberCoroutineScope()
    val activeSnackbarMessages = remember { mutableSetOf<String>() }
    val workingMessage = stringResource(Res.string.setting_backup_working)
    val localizedBackupError = uiState.errorMessage
    val localizedBackupSuccess = uiState.successMessage

    fun showSnackbarOnce(message: String) {
        if (!activeSnackbarMessages.add(message)) return
        snackbarScope.launch {
            try {
                val visibleMessages = listOfNotNull(
                    snackbarHostState.newestSnackbarData(),
                    snackbarHostState.oldestSnackbarData(),
                ).map { it.visuals.message }
                if (message !in visibleMessages) {
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = true,
                    )
                }
            } finally {
                activeSnackbarMessages.remove(message)
            }
        }
    }

    LaunchedEffect(uiState.isWorking, uiState.successMessage, uiState.errorMessage) {
        if (uiState.isWorking) {
            showSnackbarOnce(workingMessage)
        } else {
            val message = localizedBackupError ?: localizedBackupSuccess
            if (!message.isNullOrBlank()) {
                onMessageShown()
                showSnackbarOnce(message)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = pagePaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoSwitchCard(
            title = stringResource(Res.string.setting_export),
        ) {
            ArrowPreference(
                title = stringResource(Res.string.setting_export_backup),
                summary = stringResource(Res.string.setting_keep_backup_safe),
                enabled = !uiState.isWorking,
                modifier = Modifier.combinedClickable(
                    onClick = {
                        exportPassword = TextFieldValue()
                        exportPasswordConfirmation = TextFieldValue()
                        showEncryptedExportDialog = true
                    },
                    onLongClick = {
                        onExport(null)
                    }
                ),
            )
        }
        InfoSwitchCard(
            title = stringResource(Res.string.setting_import),
        ) {
            ArrowPreference(
                title = stringResource(Res.string.setting_import_backup),
                summary = stringResource(Res.string.setting_import_backup_summary),
                enabled = !uiState.isWorking,
                onClick = { onImport(BackupImportFormat.CURRENT) },
            )
            ArrowPreference(
                title = stringResource(Res.string.setting_import_legacy_backup),
                summary = stringResource(Res.string.setting_import_from_fork),
                enabled = !uiState.isWorking,
                onClick = { onImport(BackupImportFormat.LEGACY) },
            )
        }
    }

    WindowDialog(
        show = showEncryptedExportDialog,
        title = stringResource(Res.string.setting_set_backup_password),
        onDismissRequest = { showEncryptedExportDialog = false },
        onDismissFinished = {
            if (!showEncryptedExportDialog) {
                exportPassword = TextFieldValue()
                exportPasswordConfirmation = TextFieldValue()
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.setting_keep_password_safe))
            TextField(
                value = exportPassword,
                onValueChange = { exportPassword = it },
                label = stringResource(Res.string.setting_password),
                enabled = !uiState.isWorking,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { exportPasswordConfirmationFocusRequester.requestFocus() },
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            TextField(
                modifier = Modifier.focusRequester(exportPasswordConfirmationFocusRequester),
                value = exportPasswordConfirmation,
                onValueChange = { exportPasswordConfirmation = it },
                label = stringResource(Res.string.setting_confirm_password),
                enabled = !uiState.isWorking,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldColors(
                    backgroundColor = AppTheme.colorScheme.secondaryContainer,
                    labelColor = AppTheme.colorScheme.onSecondaryContainer,
                    borderColor = if (exportPasswordConfirmation.text == exportPassword.text) {
                        AppTheme.colorScheme.primary
                    } else {
                        AppTheme.colorScheme.error
                    },
                    highContrastBorderColor = AppTheme.colorScheme.outline,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_cancel),
                    onClick = { showEncryptedExportDialog = false },
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_confirm),
                    enabled = !uiState.isWorking && exportPassword.text.isNotEmpty() &&
                        exportPassword.text == exportPasswordConfirmation.text,
                    onClick = {
                        if (!uiState.isWorking && exportPassword.text.isNotEmpty() &&
                            exportPassword.text == exportPasswordConfirmation.text
                        ) {
                            val password = exportPassword.text
                            showEncryptedExportDialog = false
                            onExport(password)
                        }
                    },
                )
            }
        }
    }

    val pendingImport = uiState.pendingImport
    WindowDialog(
        show = pendingImport != null,
        title = if (pendingImport?.format == BackupImportFormat.LEGACY) {
            stringResource(Res.string.setting_import_legacy_backup)
        } else {
            stringResource(Res.string.setting_import_backup)
        },
        onDismissRequest = {
            if (!uiState.isWorking) onCancelImport()
        },
        onDismissFinished = {
            if (uiState.pendingImport == null) importPassword = ""
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (pendingImport?.format == BackupImportFormat.LEGACY) {
                    stringResource(Res.string.setting_legacy_import_warning)
                } else {
                    stringResource(Res.string.setting_import_overwrite_warning)
                },
            )
            InputValueRow(
                label = stringResource(Res.string.setting_backup_password),
                value = importPassword,
                valueLabel = stringResource(Res.string.setting_leave_blank_if_unencrypted),
                allowEdit = !uiState.isWorking,
                onValueChange = { importPassword = it },
                visualTransformation = PasswordVisualTransformation(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_cancel),
                    enabled = !uiState.isWorking,
                    onClick = onCancelImport,
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.setting_confirm_import),
                    enabled = !uiState.isWorking && pendingImport != null,
                    onClick = {
                        if (!uiState.isWorking && pendingImport != null) onConfirmImport(importPassword)
                    },
                )
            }
        }
    }
}

@Composable
private fun EditSettingItemWindowPopupArrow(
    title: String,
    valueLabel: String,
    originalValue: String,
    enabled: Boolean,
    validation: (String) -> SettingEditValidation,
    onSubmit: (String) -> Boolean,
    summary: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var showEditOverlay by rememberSaveable { mutableStateOf(false) }

    ArrowPreference(
        title = title,
        summary = summary ?: originalValue.ifBlank { valueLabel },
        enabled = enabled,
        onClick = { showEditOverlay = true }
    )

    WindowDialog(
        title = title,
        show = showEditOverlay,
        onDismissRequest = { showEditOverlay = false },
        onDismissFinished = { showEditOverlay = false },
    ) {
        var value by rememberSaveable(showEditOverlay) { mutableStateOf(originalValue) }
        val currentValidation = validation(value)

        Column ( verticalArrangement = Arrangement.spacedBy(10.dp) ) {
            TextField(
                value = value,
                label = valueLabel,
                onValueChange = { value = it },
                useLabelAsPlaceholder = true,
                enabled = enabled,
                keyboardOptions = keyboardOptions,
                visualTransformation = visualTransformation,
            )
            if (currentValidation.error != null) {
                Text(
                    text = stringResource(currentValidation.error),
                    color = AppTheme.colorScheme.error,
                    style = AppTheme.textStyles.body2,
                )
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_cancel),
                    onClick = { showEditOverlay = false }
                )
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.common_action_confirm),
                    enabled = currentValidation.canSubmit && enabled,
                    onClick = {
                        if ( onSubmit(value) ) {
                            showEditOverlay = false
                        }
                    },
                    colors = textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
private fun SettingEditError(error: StringResource?) {
    if (error != null) {
        Text(
            text = stringResource(error),
            color = AppTheme.colorScheme.error,
            style = AppTheme.textStyles.body2,
        )
    }
}

@Composable
private fun StartActionColoredIcon(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colorfulPaletteColors.e,
    imageVector: ImageVector? = null,
    vectorModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(end = 4.dp)
            .size(26.dp)
            .background(color, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        imageVector?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp).then(vectorModifier),
                tint = Color.White,
            )
        }
    }
}

private enum class TimePickerTarget {
    START,
    END,
}

private val standardExecuteScheduleTypes = listOf(
    ExecuteScheduleType.INTERVAL,
    ExecuteScheduleType.TIME_RANGE,
)

@Composable
private fun localizedWeekDayNames(): List<String> = listOf(
    stringResource(Res.string.setting_weekday_mon),
    stringResource(Res.string.setting_weekday_tue),
    stringResource(Res.string.setting_weekday_wed),
    stringResource(Res.string.setting_weekday_thu),
    stringResource(Res.string.setting_weekday_fri),
    stringResource(Res.string.setting_weekday_sat),
    stringResource(Res.string.setting_weekday_sun),
)

@Composable
private fun Set<Int>.weekdaySummary(): String {
    val names = localizedWeekDayNames()
    return when {
        isEmpty() -> stringResource(Res.string.setting_not_selected)
        size == 7 -> stringResource(Res.string.setting_every_day)
        else -> sorted().joinToString(stringResource(Res.string.setting_weekday_separator)) { day ->
            names[day - 1]
        }
    }
}

private fun formatMinuteOfDay(minuteOfDay: Int, use24HourFormat: Boolean): String {
    val hour = minuteOfDay.coerceIn(0, 1439) / 60
    val minute = minuteOfDay.coerceIn(0, 1439) % 60
    if (use24HourFormat) return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when (val halfDayHour = hour % 12) {
        0 -> 12
        else -> halfDayHour
    }
    return "$displayHour:${minute.toString().padStart(2, '0')} $period"
}

private fun String.toWifiNames(): Set<String> = split(',', '\n')
    .map(String::trim)
    .filter(String::isNotEmpty)
    .toSet()
