package moe.https.syncthing.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.eygraber.uri.Uri
import com.eygraber.uri.toKmpUriOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import moe.https.syncthing.core.GuiTlsFile
import moe.https.syncthing.core.SettingAccessMode
import moe.https.syncthing.core.SettingConfiguration
import moe.https.syncthing.core.SettingController
import moe.https.syncthing.core.SettingSaveResult
import moe.https.syncthing.core.SettingSnapshot
import moe.https.syncthing.core.retrySettingSave
import moe.https.syncthing.generated.resources.Res
import moe.https.syncthing.generated.resources.common_unknown
import moe.https.syncthing.generated.resources.setting_error_default_listen_edit
import moe.https.syncthing.generated.resources.setting_error_discovery_address_required
import moe.https.syncthing.generated.resources.setting_error_restart_core
import moe.https.syncthing.storage.AppSettingPrivateStorage
import moe.https.syncthing.ui.model.SettingFormState
import moe.https.syncthing.ui.model.SettingUiState
import moe.https.syncthing.ui.util.AutoStartCondition
import moe.https.syncthing.ui.util.AutoStartModeType
import moe.https.syncthing.ui.util.CronTrigger
import moe.https.syncthing.ui.util.ExecuteSchedule
import moe.https.syncthing.ui.util.ListenAddressListItem
import moe.https.syncthing.ui.util.ListenAddressSetting
import moe.https.syncthing.ui.util.SettingProtocolStack
import moe.https.syncthing.ui.util.UriProtocolStack
import moe.https.syncthing.ui.util.loadAutoStartCondition
import moe.https.syncthing.ui.util.normalized
import moe.https.syncthing.ui.util.saveAutoStartCondition
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

sealed interface DiscoveryServerPingState {
    data object InProgress : DiscoveryServerPingState
    data class Success(val latencyMillis: Long) : DiscoveryServerPingState
    data class Failure(val message: String) : DiscoveryServerPingState
}

class SettingViewModel(
    private val controller: SettingController,
    private val appSettingsStorage: AppSettingPrivateStorage,
    private val onAutoStartSettingsChanged: () -> Unit = {},
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingUiState())
    val uiState: StateFlow<SettingUiState> = mutableUiState.asStateFlow()
    private val operationMutex = Mutex()
    private val saveRequests = Channel<SettingSaveRequest>(Channel.UNLIMITED)
    private val saveErrorMessages = Channel<String>(Channel.UNLIMITED)
    private val saveState = SettingSaveState()
    val saveErrors: Flow<String> = saveErrorMessages.receiveAsFlow()
    private val discoveryServerPingStates = mutableStateMapOf<String, DiscoveryServerPingState>()
    var autoStartMode by mutableStateOf(
        appSettingsStorage.getString(AppSettingPrivateStorage.KEY_AUTO_START_MODE)
            ?.let { storedValue ->
                AutoStartModeType.entries.firstOrNull { it.name == storedValue }
            }
            ?: AutoStartModeType.DISABLED,
    )
        private set
    var autoStartCondition by mutableStateOf(loadAutoStartCondition(appSettingsStorage))
        private set
    private val savedListenSetting = loadSavedListenSetting()
    private var disabledRelayAddresses = savedListenSetting.relays.filterNot { it.enabled }
    private var disabledDiscoveryAddresses = loadSavedDiscoverySetting().filterNot { it.enabled }
    private var listenAddressDraft by mutableStateOf(savedListenSetting.copy(relays = emptyList()))
    var listenAddressSettingUnsaved: ListenAddressSetting
        get() = listenAddressDraft
        set(value) {
            if (!canEditSetting() || value == listenAddressDraft) return
            val before = listenAddressDraft
            listenAddressDraft = value
            val fields = buildSet {
                if (before.tcp != value.tcp) add(SettingSaveField.LISTEN_TCP)
                if (before.quic != value.quic) add(SettingSaveField.LISTEN_QUIC)
                if (before.port != value.port) add(SettingSaveField.LISTEN_PORT)
                if (before.stackPrefer != value.stackPrefer) add(SettingSaveField.LISTEN_STACK)
                if (before.relays != value.relays) add(SettingSaveField.RELAY_SERVERS)
            }
            if (SettingSaveField.LISTEN_STACK in fields && addressProtocolStack == SettingProtocolStack.CUSTOM) {
                actualListenStack = value.stackPrefer
            }
            requestSave(fields)
        }
    private var discoveryAddressDraft by mutableStateOf(emptyList<ListenAddressListItem>())
    var discoveryAddressSettingUnsaved: List<ListenAddressListItem>
        get() = discoveryAddressDraft
        set(value) {
            if (!canEditSetting() || value == discoveryAddressDraft) return
            discoveryAddressDraft = value.toList()
            requestSave(setOf(SettingSaveField.DISCOVERY_SERVERS))
        }
    var addressProtocolStack by mutableStateOf(
        appSettingsStorage.getString(AppSettingPrivateStorage.KEY_PROTOCOL_STACK)
            ?.let { storedValue ->
                SettingProtocolStack.entries.firstOrNull { it.name == storedValue }
            }
            ?: SettingProtocolStack.DUAL,
    )
        private set

    var actualListenStack by mutableStateOf(
        when (addressProtocolStack) {
            SettingProtocolStack.CUSTOM -> listenAddressSettingUnsaved.stackPrefer
            SettingProtocolStack.IPV4 -> UriProtocolStack.IPV4
            SettingProtocolStack.IPV6 -> UriProtocolStack.IPV6
            SettingProtocolStack.DUAL -> UriProtocolStack.DUAL
        }
    )
        private set
    private var confirmedListenSetting = listenAddressDraft
    private var confirmedDiscoveryAddresses = discoveryAddressDraft
    private var confirmedActualListenStack = actualListenStack
    private var confirmedProtocolStack = addressProtocolStack

    init {
        viewModelScope.launch {
            for (request in saveRequests) {
                operationMutex.withLock {
                    if (!saveState.isSuperseded(request)) saveSettingChange(request)
                }
            }
        }
    }

    private fun canEditSetting(): Boolean {
        val state = mutableUiState.value
        return state.settingRaw != null && state.accessMode != null &&
            !state.isLoading && !state.isSaving
    }

    private fun requestSave(
        fields: Set<SettingSaveField>,
        guiTlsFiles: Map<GuiTlsFile, ByteArray> = emptyMap(),
    ) {
        if (fields.isEmpty()) return
        val state = mutableUiState.value
        val setting = state.settingRaw ?: return
        val saveFields = if (SettingSaveField.AUTHENTICATION in fields &&
            state.formState.settingChangeError(setting, state.accessMode, setOf(SettingSaveField.AUTHENTICATION)) != null) {
            fields - SettingSaveField.AUTHENTICATION
        } else {
            fields
        }
        // Keep incomplete credentials in their own editor; never block another setting's request.
        if (saveFields.isEmpty()) return
        val version = saveState.edit(saveFields)
        val request = SettingSaveRequest(
            version = version,
            fields = saveFields,
            form = mergeSettingForm(SettingFormState(), state.formState, saveFields),
            listen = mergeListenSetting(confirmedListenSetting, listenAddressDraft, saveFields)
                .let { it.copy(relays = it.relays.toList()) },
            listenStack = if (SettingSaveField.LISTEN_STACK in saveFields) actualListenStack else confirmedActualListenStack,
            protocolStack = if (SettingSaveField.PROTOCOL_STACK in saveFields) addressProtocolStack else confirmedProtocolStack,
            discovery = if (SettingSaveField.DISCOVERY_SERVERS in saveFields) discoveryAddressDraft.toList() else emptyList(),
            guiTlsFiles = guiTlsFiles.mapValues { (_, content) -> content.copyOf() },
        )
        mutableUiState.update { it.copy(isSaving = true, errorMessage = null) }
        if (saveRequests.trySend(request).isFailure) finishRequest(request)
    }

    fun updateAddressProtocolStack(stack: SettingProtocolStack) {
        if (!canEditSetting() || stack == addressProtocolStack) return
        addressProtocolStack = stack
        actualListenStack = when (stack) {
            SettingProtocolStack.CUSTOM -> listenAddressDraft.stackPrefer
            SettingProtocolStack.IPV4 -> UriProtocolStack.IPV4
            SettingProtocolStack.IPV6 -> UriProtocolStack.IPV6
            SettingProtocolStack.DUAL -> UriProtocolStack.DUAL
        }
        mutableUiState.update { it.copy(formState = it.formState.copy(guiListenAddress = stack.guiListenAddress)) }
        requestSave(
            setOf(
                SettingSaveField.GUI_LISTEN_ADDRESS,
                SettingSaveField.PROTOCOL_STACK,
                SettingSaveField.LISTEN_STACK,
            ),
        )
    }

    fun onRestartPromptDismissed() {
        mutableUiState.update { it.copy(showRestartPrompt = false) }
    }

    fun restartCore() {
        viewModelScope.launch {
            operationMutex.withLock {
                if (!mutableUiState.value.restartRequired) return@withLock
                try {
                    if (controller.restartCore()) {
                        onRestartPromptDismissed()
                    } else {
                        showError(Res.string.setting_error_restart_core)
                        mutableUiState.update { it.copy(showRestartPrompt = true) }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    reportError(error.userMessageOrNull() ?: getString(Res.string.common_unknown))
                    mutableUiState.update { it.copy(showRestartPrompt = true) }
                }
            }
        }
    }

    fun discoveryServerPingState(address: String): DiscoveryServerPingState? =
        discoveryServerPingStates[address.trim()]

    fun updateAutoStartMode(mode: AutoStartModeType) {
        if (mode == autoStartMode) return
        autoStartMode = mode
        appSettingsStorage.putString(
            AppSettingPrivateStorage.KEY_AUTO_START_MODE,
            mode.name,
        )
        onAutoStartSettingsChanged()
    }

    fun updateAutoStartCondition(condition: AutoStartCondition) {
        val normalizedCondition = condition.normalized()
        if (normalizedCondition == autoStartCondition) return
        autoStartCondition = normalizedCondition
        saveAutoStartCondition(appSettingsStorage, normalizedCondition)
        onAutoStartSettingsChanged()
    }

    fun addExecuteSchedule() {
        val nextId = (autoStartCondition.schedules.maxOfOrNull(ExecuteSchedule::id) ?: 0L) + 1L
        updateAutoStartCondition(
            autoStartCondition.copy(
                schedules = autoStartCondition.schedules + ExecuteSchedule(id = nextId),
            ),
        )
    }

    fun updateExecuteSchedule(schedule: ExecuteSchedule) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                schedules = autoStartCondition.schedules.map { current ->
                    if (current.id == schedule.id) schedule else current
                },
            ),
        )
    }

    fun removeExecuteSchedule(id: Long) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                schedules = autoStartCondition.schedules.filterNot { it.id == id },
            ),
        )
    }

    fun addStartCronTrigger() {
        val nextId = (autoStartCondition.startCronTriggers.maxOfOrNull(CronTrigger::id) ?: 0L) + 1L
        updateAutoStartCondition(
            autoStartCondition.copy(
                startCronTriggers = autoStartCondition.startCronTriggers + CronTrigger(id = nextId),
            ),
        )
    }

    fun updateStartCronTrigger(trigger: CronTrigger) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                startCronTriggers = autoStartCondition.startCronTriggers.map { current ->
                    if (current.id == trigger.id) trigger else current
                },
            ),
        )
    }

    fun removeStartCronTrigger(id: Long) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                startCronTriggers = autoStartCondition.startCronTriggers.filterNot { it.id == id },
            ),
        )
    }

    fun addStopCronTrigger() {
        val nextId = (autoStartCondition.stopCronTriggers.maxOfOrNull(CronTrigger::id) ?: 0L) + 1L
        updateAutoStartCondition(
            autoStartCondition.copy(
                stopCronTriggers = autoStartCondition.stopCronTriggers + CronTrigger(
                    id = nextId,
                    expression = "0 16 * * *",
                ),
            ),
        )
    }

    fun updateStopCronTrigger(trigger: CronTrigger) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                stopCronTriggers = autoStartCondition.stopCronTriggers.map { current ->
                    if (current.id == trigger.id) trigger else current
                },
            ),
        )
    }

    fun removeStopCronTrigger(id: Long) {
        updateAutoStartCondition(
            autoStartCondition.copy(
                stopCronTriggers = autoStartCondition.stopCronTriggers.filterNot { it.id == id },
            ),
        )
    }

    fun pingDiscoveryServer(address: String) {
        val normalizedAddress = address.trim()
        if (normalizedAddress.isBlank()) {
            viewModelScope.launch {
                discoveryServerPingStates[normalizedAddress] = DiscoveryServerPingState.Failure(
                    getString(Res.string.setting_error_discovery_address_required),
                )
            }
            return
        }
        if (discoveryServerPingStates[normalizedAddress] == DiscoveryServerPingState.InProgress) {
            return
        }

        discoveryServerPingStates[normalizedAddress] = DiscoveryServerPingState.InProgress
        viewModelScope.launch {
            try {
                val latencyMillis = controller.pingDiscoveryServer(normalizedAddress)
                discoveryServerPingStates[normalizedAddress] =
                    DiscoveryServerPingState.Success(latencyMillis)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                discoveryServerPingStates[normalizedAddress] = DiscoveryServerPingState.Failure(
                    error.userMessageOrNull() ?: getString(Res.string.common_unknown),
                )
            }
        }
    }

    fun clearDiscoveryServerPingState(address: String) {
        discoveryServerPingStates.remove(address.trim())
    }


    fun onCoreUnavailable() {
        mutableUiState.update {
            it.copy(
                restartRequired = false,
                showRestartPrompt = false,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            operationMutex.withLock {
                mutableUiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
                try {
                    val snapshot = controller.loadSetting()
                    applyConfirmedSnapshot(snapshot)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val message = error.userMessageOrNull() ?: getString(Res.string.common_unknown)
                    mutableUiState.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                        )
                    }
                    reportError(message)
                }
            }
        }
    }

    internal fun submitSettingEdit(
        field: SettingEditField,
        value: String,
        unit: SettingConfiguration.DiskSpaceUnit = mutableUiState.value.formState.minHomeDiskFreeUnit,
    ): Boolean {
        val state = mutableUiState.value
        val validation = validateSettingEdit(state, field, value, unit)
        if (!validation.canSubmit) {
            validation.error?.let { error -> viewModelScope.launch { showError(error) } }
            return false
        }
        val form = field.applyTo(state.formState, value, unit)
        mutableUiState.update {
            it.copy(
                formState = form,
            )
        }
        requestSave(changedSettingFormFields(state.formState, form))
        return true
    }

    fun onFormChange(
        deviceName: String? = null,
        minHomeDiskFree: String? = null,
        minHomeDiskFreeUnit: SettingConfiguration.DiskSpaceUnit? = null,
        usageReportingEnabled: Boolean? = null,
        guiListenAddress: String? = null,
        guiPort: String? = null,
        guiPortConflictBehavior: SettingConfiguration.GuiPortConflictBehavior? = null,
        guiAuthenticationEnabled: Boolean? = null,
        guiUser: String? = null,
        newGuiPassword: String? = null,
        guiTheme: SettingConfiguration.GuiTheme? = null,
        guiUseTls: Boolean? = null,
        listenAddresses: String? = null,
        maxSendKiBPerSecond: String? = null,
        maxReceiveKiBPerSecond: String? = null,
        reconnectionIntervalSeconds: String? = null,
        limitBandwidthInLan: Boolean? = null,
        globalDiscoveryEnabled: Boolean? = null,
        globalDiscoveryServers: String? = null,
        localDiscoveryEnabled: Boolean? = null,
        localDiscoveryPort: String? = null,
        localDiscoveryMulticastAddress: String? = null,
        announceLanAddresses: Boolean? = null,
        natEnabled: Boolean? = null,
        relaysEnabled: Boolean? = null,
        alwaysLocalNetworks: String? = null,
        connectionLimitMax: String? = null,
    ) {
        if (!canEditSetting()) return
        var changedFields = emptySet<SettingSaveField>()
        mutableUiState.update { state ->
            val setting = state.settingRaw
            if (setting == null) {
                state
            } else {
                val currentFormState = state.formState
                val changedFormState = currentFormState.copy(
                    deviceName = deviceName ?: currentFormState.deviceName,
                    minHomeDiskFree = minHomeDiskFree ?: currentFormState.minHomeDiskFree,
                    minHomeDiskFreeUnit = minHomeDiskFreeUnit ?: currentFormState.minHomeDiskFreeUnit,
                    usageReportingEnabled = usageReportingEnabled ?: currentFormState.usageReportingEnabled,
                    guiListenAddress = guiListenAddress ?: currentFormState.guiListenAddress,
                    guiPort = guiPort ?: currentFormState.guiPort,
                    guiPortConflictBehavior =
                        guiPortConflictBehavior ?: currentFormState.guiPortConflictBehavior,
                    guiAuthenticationEnabled =
                        guiAuthenticationEnabled ?: currentFormState.guiAuthenticationEnabled,
                    guiUser = guiUser ?: currentFormState.guiUser,
                    newGuiPassword = newGuiPassword ?: currentFormState.newGuiPassword,
                    guiTheme = guiTheme ?: currentFormState.guiTheme,
                    guiUseTls = guiUseTls ?: currentFormState.guiUseTls,
                    listenAddresses = listenAddresses ?: currentFormState.listenAddresses,
                    maxSendKiBPerSecond =
                        maxSendKiBPerSecond ?: currentFormState.maxSendKiBPerSecond,
                    maxReceiveKiBPerSecond =
                        maxReceiveKiBPerSecond ?: currentFormState.maxReceiveKiBPerSecond,
                    reconnectionIntervalSeconds =
                        reconnectionIntervalSeconds ?: currentFormState.reconnectionIntervalSeconds,
                    limitBandwidthInLan = limitBandwidthInLan ?: currentFormState.limitBandwidthInLan,
                    globalDiscoveryEnabled =
                        globalDiscoveryEnabled ?: currentFormState.globalDiscoveryEnabled,
                    globalDiscoveryServers =
                        globalDiscoveryServers ?: currentFormState.globalDiscoveryServers,
                    localDiscoveryEnabled =
                        localDiscoveryEnabled ?: currentFormState.localDiscoveryEnabled,
                    localDiscoveryPort = localDiscoveryPort ?: currentFormState.localDiscoveryPort,
                    localDiscoveryMulticastAddress =
                        localDiscoveryMulticastAddress ?: currentFormState.localDiscoveryMulticastAddress,
                    announceLanAddresses = announceLanAddresses ?: currentFormState.announceLanAddresses,
                    natEnabled = natEnabled ?: currentFormState.natEnabled,
                    relaysEnabled = relaysEnabled ?: currentFormState.relaysEnabled,
                    alwaysLocalNetworks = alwaysLocalNetworks ?: currentFormState.alwaysLocalNetworks,
                    connectionLimitMax = connectionLimitMax ?: currentFormState.connectionLimitMax,
                )
                changedFields = changedSettingFormFields(currentFormState, changedFormState)
                if (changedFormState == currentFormState) {
                    state
                } else {
                    state.copy(
                        formState = changedFormState,
                    )
                }
            }
        }
        requestSave(changedFields)
    }

    fun stageGuiTlsFile(type: GuiTlsFile, content: ByteArray) {
        if (!canEditSetting()) return
        mutableUiState.update {
            it.copy(
                errorMessage = null,
                selectedGuiTlsFile = type,
            )
        }
        val field = when (type) {
            GuiTlsFile.CERTIFICATE -> SettingSaveField.TLS_CERTIFICATE
            GuiTlsFile.PRIVATE_KEY -> SettingSaveField.TLS_PRIVATE_KEY
        }
        requestSave(setOf(field), mapOf(type to content))
    }

    fun onNoticeMessageShown() {
        mutableUiState.update { it.copy(selectedGuiTlsFile = null) }
    }

    fun reportError(message: String) {
        mutableUiState.update {
            it.copy(errorMessage = message, selectedGuiTlsFile = null)
        }
        saveErrorMessages.trySend(message)
    }

    private suspend fun saveSettingChange(request: SettingSaveRequest) {
        val state = mutableUiState.value
        val previous = state.settingRaw ?: run {
            finishRequest(request)
            return
        }
        val fields = request.fields
        val listen = mergeListenSetting(confirmedListenSetting, request.listen, fields)
        val listenStack = if (SettingSaveField.LISTEN_STACK in fields) request.listenStack else confirmedActualListenStack
        val discovery = if (SettingSaveField.DISCOVERY_SERVERS in fields) request.discovery else confirmedDiscoveryAddresses
        val directListenChanged = state.accessMode != SettingAccessMode.STARTUP_ONLY &&
            fields.any { it in directListenFields } &&
            (listen.copy(relays = emptyList()) != confirmedListenSetting.copy(relays = emptyList()) ||
                listenStack != confirmedActualListenStack)
        if (directListenChanged && listen.relays.any { it.enabled && it.uri == "default" }) {
            finishRequest(request)
            showError(Res.string.setting_error_default_listen_edit)
            return
        }
        var form = mergeSettingForm(previous.toFormState(), request.form, fields)
        if (fields.any { it in directListenFields || it == SettingSaveField.RELAY_SERVERS }) {
            form = form.copy(listenAddresses = buildListenAddresses(
                previous.listenAddresses,
                getListenAddressStringFromUnsaved(listen, listenStack).toValues(),
                listen.relays,
                directListenChanged,
            ).joinToString(", "))
        }
        if (SettingSaveField.DISCOVERY_SERVERS in fields) {
            form = form.copy(globalDiscoveryServers = getDiscoveryAddressStringFromUnsaved(discovery))
        }
        val validationError = form.settingChangeError(previous, state.accessMode, fields)
        if (validationError != null) {
            finishRequest(request)
            showError(validationError)
            return
        }
        val configuration = buildSettingConfiguration(previous, form, fields)
        try {
            val result = if (configuration != previous || request.guiTlsFiles.isNotEmpty()) {
                retrySettingSave {
                    controller.saveSettingChange(previous, configuration, request.guiTlsFiles)
                }
            } else {
                SettingSaveResult(restartRequired = false, accessMode = requireNotNull(state.accessMode))
            }
            val confirmed = configuration.copy(
                guiPasswordConfigured = if (result.accessMode == SettingAccessMode.STARTUP_ONLY) {
                    configuration.guiPasswordConfigured
                } else {
                    configuration.guiPasswordConfigured || configuration.guiAuthenticationEnabled
                },
                newGuiPassword = "",
            )
            confirmRequestPreferences(request, listen, listenStack, discovery)
            saveState.finish(request)
            applyConfirmedSnapshot(SettingSnapshot(confirmed, result.accessMode))
            mutableUiState.update { current ->
                val restartNeeded = result.restartRequired && result.accessMode == SettingAccessMode.REST
                current.copy(
                    isSaving = false,
                    restartRequired = current.restartRequired || restartNeeded,
                    showRestartPrompt = current.showRestartPrompt || restartNeeded,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            finishRequest(request)
            val message = error.userMessageOrNull() ?: getString(Res.string.common_unknown)
            reportError(message)
        }
    }

    private fun finishRequest(request: SettingSaveRequest) {
        saveState.finish(request)
        val state = mutableUiState.value
        state.settingRaw?.let(::mergeConfirmedDrafts)
        mutableUiState.update { it.copy(isSaving = false) }
    }

    private fun confirmRequestPreferences(
        request: SettingSaveRequest,
        listen: ListenAddressSetting,
        listenStack: UriProtocolStack,
        discovery: List<ListenAddressListItem>,
    ) {
        val fields = request.fields
        if (fields.any { it in directListenFields || it == SettingSaveField.RELAY_SERVERS }) {
            confirmedListenSetting = listen
            confirmedActualListenStack = listenStack
            disabledRelayAddresses = listen.relays.filterNot { it.enabled }
            appSettingsStorage.putString(
                AppSettingPrivateStorage.KEY_LISTEN_PREFERENCE,
                Json.encodeToString(listen.trim().copy(relays = disabledRelayAddresses)),
            )
        }
        if (SettingSaveField.DISCOVERY_SERVERS in fields) {
            confirmedDiscoveryAddresses = discovery
            disabledDiscoveryAddresses = discovery.filterNot { it.enabled }
            appSettingsStorage.putString(
                AppSettingPrivateStorage.KEY_DISCOVERY_PREFERENCE,
                Json.encodeToString(disabledDiscoveryAddresses.trim()),
            )
        }
        if (SettingSaveField.PROTOCOL_STACK in fields) {
            confirmedProtocolStack = request.protocolStack
            appSettingsStorage.putString(AppSettingPrivateStorage.KEY_PROTOCOL_STACK, confirmedProtocolStack.name)
        }
    }

    private fun applyConfirmedSnapshot(snapshot: SettingSnapshot) {
        confirmedListenSetting = confirmedListenSetting.copy(relays = mergeCoreServerAddresses(
            snapshot.configuration.listenAddresses.filter(::isRelayOrDefaultAddress),
            disabledRelayAddresses,
        ))
        confirmedDiscoveryAddresses = mergeCoreServerAddresses(
            snapshot.configuration.globalDiscoveryServers,
            disabledDiscoveryAddresses,
        )
        mutableUiState.update {
            it.copy(
                settingRaw = snapshot.configuration,
                accessMode = snapshot.accessMode,
                isLoading = false,
                hasLoaded = true,
                errorMessage = null,
            )
        }
        mergeConfirmedDrafts(snapshot.configuration)
    }

    private fun mergeConfirmedDrafts(configuration: SettingConfiguration) {
        val fields = saveState.pendingFields
        listenAddressDraft = mergeListenSetting(confirmedListenSetting, listenAddressDraft, fields)
        if (SettingSaveField.DISCOVERY_SERVERS !in fields) discoveryAddressDraft = confirmedDiscoveryAddresses
        if (SettingSaveField.PROTOCOL_STACK !in fields) addressProtocolStack = confirmedProtocolStack
        if (SettingSaveField.LISTEN_STACK !in fields) actualListenStack = confirmedActualListenStack
        mutableUiState.update { state ->
            val form = mergeSettingForm(configuration.toFormState(), state.formState, fields)
            state.copy(formState = form)
        }
    }

    fun loadSavedListenSetting(): ListenAddressSetting {
        return runCatching {
            Json.decodeFromString<ListenAddressSetting>(
                appSettingsStorage.getString( AppSettingPrivateStorage.KEY_LISTEN_PREFERENCE)!!
            )
        }.getOrDefault( ListenAddressSetting(
            stackPrefer = UriProtocolStack.DUAL,
            tcp = true,
            quic = true,
            port = 22000,
            relays = emptyList(),
        ))
    }

    private suspend fun showError(resource: StringResource) {
        reportError(getString(resource))
    }

    fun loadSavedDiscoverySetting(): List<ListenAddressListItem> {
        return runCatching {
            Json.decodeFromString<List<ListenAddressListItem>>(
                appSettingsStorage.getString( AppSettingPrivateStorage.KEY_DISCOVERY_PREFERENCE)!!
            )
        }.getOrDefault(emptyList())
    }

    fun getListenAddressStringFromUnsaved(
        listenAddressSetting: ListenAddressSetting,
        listenStack: UriProtocolStack = actualListenStack,
    ): String {
        val result = mutableListOf<String>()

        if ( ifStackFits( listenStack, UriProtocolStack.IPV4 ) ) {
            if ( listenAddressSetting.tcp ) result += ("tcp4://0.0.0.0:" + listenAddressSetting.port.toString())
            if ( listenAddressSetting.quic ) result += ("quic4://0.0.0.0:" + listenAddressSetting.port.toString())
        }
        if ( ifStackFits( listenStack, UriProtocolStack.IPV6 ) ) {
            if ( listenAddressSetting.tcp ) result += ("tcp6://[::]:" + listenAddressSetting.port.toString())
            if ( listenAddressSetting.quic ) result += ("quic6://[::]:" + listenAddressSetting.port.toString())
        }

        for (item in listenAddressSetting.relays) {
            if (item.enabled) { result += item.uri }
        }

        return result.joinToString(", ")
    }

    fun getDiscoveryAddressStringFromUnsaved(listenAddressListItem: List<ListenAddressListItem>): String {
        val result = mutableListOf<String>()

        for (item in listenAddressListItem) {
            if (item.enabled) { result += item.uri }
        }

        return result.joinToString(", ")
    }

    fun listenRelayAddressValidator(address: String): Boolean {
        return isRelayOrDefaultAddress(address)
    }

    fun ifStackFits(parent: UriProtocolStack, item: UriProtocolStack): Boolean {
        return when {
            ( parent == UriProtocolStack.DUAL ) || ( item == UriProtocolStack.DUAL ) || ( parent == item ) -> true
            else -> false
        }
    }

    companion object {
        fun factory(
            controller: SettingController,
            appSettingsStorage: AppSettingPrivateStorage,
            onAutoStartSettingsChanged: () -> Unit = {},
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingViewModel(
                    controller = controller,
                    appSettingsStorage = appSettingsStorage,
                    onAutoStartSettingsChanged = onAutoStartSettingsChanged,
                )
            }
        }
    }
}

internal fun isRelayOrDefaultAddress(address: String): Boolean =
    address == "default" || address.startsWith("relay://") ||
        address.startsWith("dynamic+http://") || address.startsWith("dynamic+https://")

internal fun mergeCoreServerAddresses(
    coreAddresses: List<String>,
    disabledAddresses: List<ListenAddressListItem>,
): List<ListenAddressListItem> =
    coreAddresses.map { ListenAddressListItem(enabled = true, uri = it) } +
        disabledAddresses.filter { disabled ->
            !disabled.enabled && disabled.uri !in coreAddresses
        }

internal fun buildListenAddresses(
    coreAddresses: List<String>,
    generatedAddresses: List<String>,
    relayAddresses: List<ListenAddressListItem>,
    directListenChanged: Boolean,
): List<String> {
    val enabledRelays = relayAddresses.filter { it.enabled }.map { it.uri }
    if (!directListenChanged && enabledRelays == coreAddresses.filter(::isRelayOrDefaultAddress)) {
        return coreAddresses
    }
    val coreDirectAddresses = coreAddresses.filterNot(::isRelayOrDefaultAddress)
    val generatedDirectAddresses = generatedAddresses.filterNot(::isRelayOrDefaultAddress)
    val directAddresses = when {
        "default" in coreAddresses && "default" !in enabledRelays ->
            (generatedDirectAddresses + coreDirectAddresses).distinct()
        directListenChanged -> generatedDirectAddresses
        else -> coreDirectAddresses
    }
    return directAddresses + enabledRelays
}

private fun ListenAddressSetting.trim(): ListenAddressSetting =
    copy(
        relays = relays.map { relayItem ->
            relayItem.copy(uri = ( rebuildUriOrNot(relayItem.uri)) )
        }
    )

private fun List<ListenAddressListItem>.trim(): List<ListenAddressListItem> =
    map { item ->
        item.copy(
            uri = rebuildUriOrNot(item.uri),
        )
    }

private fun rebuildUriOrNot(raw: String): String {
    val parsed = raw.trim().toKmpUriOrNull() ?: return raw
    val scheme = parsed.scheme?.lowercase() ?: return raw
    val authority = parsed.encodedAuthority ?: return raw

    if (!parsed.isHierarchical || parsed.host.isNullOrBlank()) {
        return raw
    }

    return Uri.Builder()
        .scheme(scheme)
        .encodedAuthority(authority)
        .path(parsed.path)
        .encodedQuery(parsed.encodedQuery)
        .fragment(parsed.fragment)
        .build()
        .toString()
}

internal fun SettingConfiguration.toFormState(): SettingFormState {
    val defaults = SettingConfiguration.startupDefaults()
    return SettingFormState(
        deviceName = deviceName,
        minHomeDiskFree = minHomeDiskFree.editableStringUnless(defaults.minHomeDiskFree),
        minHomeDiskFreeUnit = minHomeDiskFreeUnit,
        usageReportingEnabled = usageReportingEnabled,
        guiListenAddress = guiListenAddress,
        guiPort = guiPort.editableStringUnless(defaults.guiPort),
        guiPortConflictBehavior = guiPortConflictBehavior,
        guiAuthenticationEnabled = guiAuthenticationEnabled,
        guiUser = guiUser,
        newGuiPassword = "",
        guiTheme = guiTheme,
        guiUseTls = guiUseTls,
        listenAddresses = listenAddresses.joinToString(", "),
        globalDiscoveryServers = globalDiscoveryServers.joinToString(", "),
        maxSendKiBPerSecond = maxSendKiBPerSecond.editableStringUnless(defaults.maxSendKiBPerSecond),
        maxReceiveKiBPerSecond = maxReceiveKiBPerSecond.editableStringUnless(defaults.maxReceiveKiBPerSecond),
        reconnectionIntervalSeconds =
            reconnectionIntervalSeconds.editableStringUnless(defaults.reconnectionIntervalSeconds),
        limitBandwidthInLan = limitBandwidthInLan,
        globalDiscoveryEnabled = globalDiscoveryEnabled,
        localDiscoveryEnabled = localDiscoveryEnabled,
        localDiscoveryPort = localDiscoveryPort.editableStringUnless(defaults.localDiscoveryPort),
        localDiscoveryMulticastAddress =
            localDiscoveryMulticastAddress.takeUnless { it == defaults.localDiscoveryMulticastAddress }.orEmpty(),
        announceLanAddresses = announceLanAddresses,
        natEnabled = natEnabled,
        relaysEnabled = relaysEnabled,
        alwaysLocalNetworks = alwaysLocalNetworks.editableStringUnless(defaults.alwaysLocalNetworks),
        connectionLimitMax = connectionLimitMax.editableStringUnless(defaults.connectionLimitMax),
    )
}

private fun Double.editableString(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()

private fun Double.editableStringUnless(defaultValue: Double): String =
    takeUnless { it == defaultValue }?.editableString().orEmpty()

private fun Int.editableStringUnless(defaultValue: Int): String =
    takeUnless { it == defaultValue }?.toString().orEmpty()

private fun List<String>.editableStringUnless(defaultValue: List<String>): String =
    takeUnless { it == defaultValue }?.joinToString("\n").orEmpty()

internal fun SettingFormState.toConfiguration(setting: SettingConfiguration): SettingConfiguration {
    val defaults = SettingConfiguration.startupDefaults()
    return setting.copy(
        deviceName = deviceName,
        minHomeDiskFree = minHomeDiskFree.toDoubleOrNull() ?: defaults.minHomeDiskFree,
        minHomeDiskFreeUnit = minHomeDiskFreeUnit,
        usageReportingEnabled = usageReportingEnabled,
        guiListenAddress = guiListenAddress,
        guiPort = guiPort.toIntOrNull() ?: defaults.guiPort,
        guiPortConflictBehavior = guiPortConflictBehavior,
        guiAuthenticationEnabled = guiAuthenticationEnabled,
        guiUser = guiUser,
        newGuiPassword = if (guiAuthenticationEnabled) newGuiPassword else "",
        guiTheme = guiTheme,
        guiUseTls = guiUseTls,
        listenAddresses = listenAddresses.toValues(),
        maxSendKiBPerSecond = maxSendKiBPerSecond.toIntOrNull() ?: defaults.maxSendKiBPerSecond,
        maxReceiveKiBPerSecond = maxReceiveKiBPerSecond.toIntOrNull() ?: defaults.maxReceiveKiBPerSecond,
        reconnectionIntervalSeconds =
            reconnectionIntervalSeconds.toIntOrNull() ?: defaults.reconnectionIntervalSeconds,
        limitBandwidthInLan = limitBandwidthInLan,
        globalDiscoveryEnabled = globalDiscoveryEnabled,
        globalDiscoveryServers = globalDiscoveryServers.toValues(),
        localDiscoveryEnabled = localDiscoveryEnabled,
        localDiscoveryPort = localDiscoveryPort.toIntOrNull() ?: defaults.localDiscoveryPort,
        localDiscoveryMulticastAddress =
            localDiscoveryMulticastAddress.ifBlank { defaults.localDiscoveryMulticastAddress },
        announceLanAddresses = announceLanAddresses,
        natEnabled = natEnabled,
        relaysEnabled = relaysEnabled,
        alwaysLocalNetworks = alwaysLocalNetworks.toValues().ifEmpty { defaults.alwaysLocalNetworks },
        connectionLimitMax = connectionLimitMax.toIntOrNull() ?: defaults.connectionLimitMax,
    )
}

private fun String.toValues(): List<String> = split(',', '\n')
    .map(String::trim)
    .filter(String::isNotBlank)

internal fun SettingConfiguration.trim(): SettingConfiguration = copy(
    deviceName = deviceName.trim(),
    guiListenAddress = guiListenAddress.trim().removePrefix("[").removeSuffix("]"),
    guiUser = guiUser.trim(),
    listenAddresses = listenAddresses.normalizedValues(),
    globalDiscoveryServers = globalDiscoveryServers.normalizedValues(),
    localDiscoveryMulticastAddress = localDiscoveryMulticastAddress.trim(),
    alwaysLocalNetworks = alwaysLocalNetworks.normalizedValues(),
)

private fun List<String>.normalizedValues(): List<String> = map(String::trim)
    .filter(String::isNotBlank)
    .distinct()

private fun Throwable.userMessageOrNull(): String? =
    message?.takeIf(String::isNotBlank)
        ?: this::class.simpleName?.takeIf(String::isNotBlank)
