package moe.https.syncthing.viewmodel

import moe.https.syncthing.core.GuiTlsFile
import moe.https.syncthing.core.SettingConfiguration
import moe.https.syncthing.ui.model.SettingFormState
import moe.https.syncthing.ui.util.ListenAddressListItem
import moe.https.syncthing.ui.util.ListenAddressSetting
import moe.https.syncthing.ui.util.SettingProtocolStack
import moe.https.syncthing.ui.util.UriProtocolStack

/** Fields that must be committed together, independently of other editor drafts. */
internal enum class SettingSaveField {
    DEVICE_NAME,
    DISK_SPACE,
    USAGE_REPORTING,
    GUI_LISTEN_ADDRESS,
    GUI_PORT,
    GUI_PORT_CONFLICT,
    AUTHENTICATION,
    GUI_THEME,
    GUI_TLS,
    LISTEN_ADDRESSES,
    UPLOAD_LIMIT,
    DOWNLOAD_LIMIT,
    RECONNECT_INTERVAL,
    LAN_BANDWIDTH,
    GLOBAL_DISCOVERY,
    DISCOVERY_ADDRESSES,
    LOCAL_DISCOVERY,
    DISCOVERY_PORT,
    DISCOVERY_MULTICAST,
    ANNOUNCE_LAN,
    NAT,
    RELAYS,
    LAN_SUBNETS,
    MAX_CONNECTIONS,
    LISTEN_TCP,
    LISTEN_QUIC,
    LISTEN_PORT,
    LISTEN_STACK,
    RELAY_SERVERS,
    DISCOVERY_SERVERS,
    PROTOCOL_STACK,
    TLS_CERTIFICATE,
    TLS_PRIVATE_KEY,
}

internal data class SettingSaveRequest(
    val version: Long,
    val fields: Set<SettingSaveField>,
    val form: SettingFormState,
    val listen: ListenAddressSetting,
    val listenStack: UriProtocolStack,
    val protocolStack: SettingProtocolStack,
    val discovery: List<ListenAddressListItem>,
    val guiTlsFiles: Map<GuiTlsFile, ByteArray> = emptyMap(),
)

/** Versions distinguish a later edit even when its value equals an earlier submission. */
internal class SettingSaveState {
    private var nextVersion = 0L
    private val pendingVersions = mutableMapOf<SettingSaveField, Long>()

    val pendingFields: Set<SettingSaveField> get() = pendingVersions.keys.toSet()

    fun isSuperseded(request: SettingSaveRequest): Boolean = request.fields.all { field ->
        pendingVersions[field]?.let { it > request.version } == true
    }

    fun edit(fields: Set<SettingSaveField>): Long {
        val version = ++nextVersion
        fields.forEach { pendingVersions[it] = version }
        return version
    }

    fun finish(request: SettingSaveRequest) {
        request.fields.forEach { field ->
            if (pendingVersions[field] == request.version) pendingVersions.remove(field)
        }
    }
}

internal fun changedSettingFormFields(
    before: SettingFormState,
    after: SettingFormState,
): Set<SettingSaveField> = buildSet {
    if (before.deviceName != after.deviceName) add(SettingSaveField.DEVICE_NAME)
    if (before.minHomeDiskFree != after.minHomeDiskFree || before.minHomeDiskFreeUnit != after.minHomeDiskFreeUnit) add(SettingSaveField.DISK_SPACE)
    if (before.usageReportingEnabled != after.usageReportingEnabled) add(SettingSaveField.USAGE_REPORTING)
    if (before.guiListenAddress != after.guiListenAddress) add(SettingSaveField.GUI_LISTEN_ADDRESS)
    if (before.guiPort != after.guiPort) add(SettingSaveField.GUI_PORT)
    if (before.guiPortConflictBehavior != after.guiPortConflictBehavior) add(SettingSaveField.GUI_PORT_CONFLICT)
    if (before.guiAuthenticationEnabled != after.guiAuthenticationEnabled || before.guiUser != after.guiUser || before.newGuiPassword != after.newGuiPassword) add(SettingSaveField.AUTHENTICATION)
    if (before.guiTheme != after.guiTheme) add(SettingSaveField.GUI_THEME)
    if (before.guiUseTls != after.guiUseTls) add(SettingSaveField.GUI_TLS)
    if (before.listenAddresses != after.listenAddresses) add(SettingSaveField.LISTEN_ADDRESSES)
    if (before.maxSendKiBPerSecond != after.maxSendKiBPerSecond) add(SettingSaveField.UPLOAD_LIMIT)
    if (before.maxReceiveKiBPerSecond != after.maxReceiveKiBPerSecond) add(SettingSaveField.DOWNLOAD_LIMIT)
    if (before.reconnectionIntervalSeconds != after.reconnectionIntervalSeconds) add(SettingSaveField.RECONNECT_INTERVAL)
    if (before.limitBandwidthInLan != after.limitBandwidthInLan) add(SettingSaveField.LAN_BANDWIDTH)
    if (before.globalDiscoveryEnabled != after.globalDiscoveryEnabled) add(SettingSaveField.GLOBAL_DISCOVERY)
    if (before.globalDiscoveryServers != after.globalDiscoveryServers) add(SettingSaveField.DISCOVERY_ADDRESSES)
    if (before.localDiscoveryEnabled != after.localDiscoveryEnabled) add(SettingSaveField.LOCAL_DISCOVERY)
    if (before.localDiscoveryPort != after.localDiscoveryPort) add(SettingSaveField.DISCOVERY_PORT)
    if (before.localDiscoveryMulticastAddress != after.localDiscoveryMulticastAddress) add(SettingSaveField.DISCOVERY_MULTICAST)
    if (before.announceLanAddresses != after.announceLanAddresses) add(SettingSaveField.ANNOUNCE_LAN)
    if (before.natEnabled != after.natEnabled) add(SettingSaveField.NAT)
    if (before.relaysEnabled != after.relaysEnabled) add(SettingSaveField.RELAYS)
    if (before.alwaysLocalNetworks != after.alwaysLocalNetworks) add(SettingSaveField.LAN_SUBNETS)
    if (before.connectionLimitMax != after.connectionLimitMax) add(SettingSaveField.MAX_CONNECTIONS)
}

/** Only selected fields are copied; unsubmitted drafts cannot enter the saved configuration. */
internal fun mergeSettingForm(
    base: SettingFormState,
    edits: SettingFormState,
    fields: Set<SettingSaveField>,
): SettingFormState = base.copy(
    deviceName = fields.select(SettingSaveField.DEVICE_NAME, base.deviceName, edits.deviceName),
    minHomeDiskFree = fields.select(SettingSaveField.DISK_SPACE, base.minHomeDiskFree, edits.minHomeDiskFree),
    minHomeDiskFreeUnit = fields.select(SettingSaveField.DISK_SPACE, base.minHomeDiskFreeUnit, edits.minHomeDiskFreeUnit),
    usageReportingEnabled = fields.select(SettingSaveField.USAGE_REPORTING, base.usageReportingEnabled, edits.usageReportingEnabled),
    guiListenAddress = fields.select(SettingSaveField.GUI_LISTEN_ADDRESS, base.guiListenAddress, edits.guiListenAddress),
    guiPort = fields.select(SettingSaveField.GUI_PORT, base.guiPort, edits.guiPort),
    guiPortConflictBehavior = fields.select(SettingSaveField.GUI_PORT_CONFLICT, base.guiPortConflictBehavior, edits.guiPortConflictBehavior),
    guiAuthenticationEnabled = fields.select(SettingSaveField.AUTHENTICATION, base.guiAuthenticationEnabled, edits.guiAuthenticationEnabled),
    guiUser = fields.select(SettingSaveField.AUTHENTICATION, base.guiUser, edits.guiUser),
    newGuiPassword = fields.select(SettingSaveField.AUTHENTICATION, base.newGuiPassword, edits.newGuiPassword),
    guiTheme = fields.select(SettingSaveField.GUI_THEME, base.guiTheme, edits.guiTheme),
    guiUseTls = fields.select(SettingSaveField.GUI_TLS, base.guiUseTls, edits.guiUseTls),
    listenAddresses = fields.select(SettingSaveField.LISTEN_ADDRESSES, base.listenAddresses, edits.listenAddresses),
    maxSendKiBPerSecond = fields.select(SettingSaveField.UPLOAD_LIMIT, base.maxSendKiBPerSecond, edits.maxSendKiBPerSecond),
    maxReceiveKiBPerSecond = fields.select(SettingSaveField.DOWNLOAD_LIMIT, base.maxReceiveKiBPerSecond, edits.maxReceiveKiBPerSecond),
    reconnectionIntervalSeconds = fields.select(SettingSaveField.RECONNECT_INTERVAL, base.reconnectionIntervalSeconds, edits.reconnectionIntervalSeconds),
    limitBandwidthInLan = fields.select(SettingSaveField.LAN_BANDWIDTH, base.limitBandwidthInLan, edits.limitBandwidthInLan),
    globalDiscoveryEnabled = fields.select(SettingSaveField.GLOBAL_DISCOVERY, base.globalDiscoveryEnabled, edits.globalDiscoveryEnabled),
    globalDiscoveryServers = fields.select(SettingSaveField.DISCOVERY_ADDRESSES, base.globalDiscoveryServers, edits.globalDiscoveryServers),
    localDiscoveryEnabled = fields.select(SettingSaveField.LOCAL_DISCOVERY, base.localDiscoveryEnabled, edits.localDiscoveryEnabled),
    localDiscoveryPort = fields.select(SettingSaveField.DISCOVERY_PORT, base.localDiscoveryPort, edits.localDiscoveryPort),
    localDiscoveryMulticastAddress = fields.select(SettingSaveField.DISCOVERY_MULTICAST, base.localDiscoveryMulticastAddress, edits.localDiscoveryMulticastAddress),
    announceLanAddresses = fields.select(SettingSaveField.ANNOUNCE_LAN, base.announceLanAddresses, edits.announceLanAddresses),
    natEnabled = fields.select(SettingSaveField.NAT, base.natEnabled, edits.natEnabled),
    relaysEnabled = fields.select(SettingSaveField.RELAYS, base.relaysEnabled, edits.relaysEnabled),
    alwaysLocalNetworks = fields.select(SettingSaveField.LAN_SUBNETS, base.alwaysLocalNetworks, edits.alwaysLocalNetworks),
    connectionLimitMax = fields.select(SettingSaveField.MAX_CONNECTIONS, base.connectionLimitMax, edits.connectionLimitMax),
)

internal fun mergeListenSetting(
    base: ListenAddressSetting,
    edits: ListenAddressSetting,
    fields: Set<SettingSaveField>,
): ListenAddressSetting = base.copy(
    tcp = if (SettingSaveField.LISTEN_TCP in fields) edits.tcp else base.tcp,
    quic = if (SettingSaveField.LISTEN_QUIC in fields) edits.quic else base.quic,
    port = if (SettingSaveField.LISTEN_PORT in fields) edits.port else base.port,
    stackPrefer = if (SettingSaveField.LISTEN_STACK in fields) edits.stackPrefer else base.stackPrefer,
    relays = if (SettingSaveField.RELAY_SERVERS in fields) edits.relays else base.relays,
)

internal val directListenFields = setOf(
    SettingSaveField.LISTEN_TCP,
    SettingSaveField.LISTEN_QUIC,
    SettingSaveField.LISTEN_PORT,
    SettingSaveField.LISTEN_STACK,
)

/** Normalize only this operation's fields, retaining all other confirmed values verbatim. */
internal fun buildSettingConfiguration(
    confirmed: SettingConfiguration,
    form: SettingFormState,
    fields: Set<SettingSaveField>,
): SettingConfiguration {
    val candidate = form.toConfiguration(confirmed).trim()
    val appliedFields = fields + buildSet {
        if (SettingSaveField.RELAY_SERVERS in fields || fields.any { it in directListenFields }) {
            add(SettingSaveField.LISTEN_ADDRESSES)
        }
        if (SettingSaveField.DISCOVERY_SERVERS in fields) add(SettingSaveField.DISCOVERY_ADDRESSES)
    }
    return confirmed.copy(
        deviceName = appliedFields.select(SettingSaveField.DEVICE_NAME, confirmed.deviceName, candidate.deviceName),
        minHomeDiskFree = appliedFields.select(SettingSaveField.DISK_SPACE, confirmed.minHomeDiskFree, candidate.minHomeDiskFree),
        minHomeDiskFreeUnit = appliedFields.select(
            SettingSaveField.DISK_SPACE, confirmed.minHomeDiskFreeUnit, candidate.minHomeDiskFreeUnit,
        ),
        usageReportingEnabled = appliedFields.select(
            SettingSaveField.USAGE_REPORTING, confirmed.usageReportingEnabled, candidate.usageReportingEnabled,
        ),
        guiListenAddress = appliedFields.select(
            SettingSaveField.GUI_LISTEN_ADDRESS, confirmed.guiListenAddress, candidate.guiListenAddress,
        ),
        guiPort = appliedFields.select(SettingSaveField.GUI_PORT, confirmed.guiPort, candidate.guiPort),
        guiPortConflictBehavior = appliedFields.select(
            SettingSaveField.GUI_PORT_CONFLICT, confirmed.guiPortConflictBehavior, candidate.guiPortConflictBehavior,
        ),
        guiAuthenticationEnabled = appliedFields.select(
            SettingSaveField.AUTHENTICATION, confirmed.guiAuthenticationEnabled, candidate.guiAuthenticationEnabled,
        ),
        guiUser = appliedFields.select(SettingSaveField.AUTHENTICATION, confirmed.guiUser, candidate.guiUser),
        newGuiPassword = appliedFields.select(
            SettingSaveField.AUTHENTICATION, confirmed.newGuiPassword, candidate.newGuiPassword,
        ),
        guiTheme = appliedFields.select(SettingSaveField.GUI_THEME, confirmed.guiTheme, candidate.guiTheme),
        guiUseTls = appliedFields.select(SettingSaveField.GUI_TLS, confirmed.guiUseTls, candidate.guiUseTls),
        listenAddresses = appliedFields.select(
            SettingSaveField.LISTEN_ADDRESSES, confirmed.listenAddresses, candidate.listenAddresses,
        ),
        maxSendKiBPerSecond = appliedFields.select(
            SettingSaveField.UPLOAD_LIMIT, confirmed.maxSendKiBPerSecond, candidate.maxSendKiBPerSecond,
        ),
        maxReceiveKiBPerSecond = appliedFields.select(
            SettingSaveField.DOWNLOAD_LIMIT, confirmed.maxReceiveKiBPerSecond, candidate.maxReceiveKiBPerSecond,
        ),
        reconnectionIntervalSeconds = appliedFields.select(
            SettingSaveField.RECONNECT_INTERVAL, confirmed.reconnectionIntervalSeconds, candidate.reconnectionIntervalSeconds,
        ),
        limitBandwidthInLan = appliedFields.select(
            SettingSaveField.LAN_BANDWIDTH, confirmed.limitBandwidthInLan, candidate.limitBandwidthInLan,
        ),
        globalDiscoveryEnabled = appliedFields.select(
            SettingSaveField.GLOBAL_DISCOVERY, confirmed.globalDiscoveryEnabled, candidate.globalDiscoveryEnabled,
        ),
        globalDiscoveryServers = appliedFields.select(
            SettingSaveField.DISCOVERY_ADDRESSES, confirmed.globalDiscoveryServers, candidate.globalDiscoveryServers,
        ),
        localDiscoveryEnabled = appliedFields.select(
            SettingSaveField.LOCAL_DISCOVERY, confirmed.localDiscoveryEnabled, candidate.localDiscoveryEnabled,
        ),
        localDiscoveryPort = appliedFields.select(
            SettingSaveField.DISCOVERY_PORT, confirmed.localDiscoveryPort, candidate.localDiscoveryPort,
        ),
        localDiscoveryMulticastAddress = appliedFields.select(
            SettingSaveField.DISCOVERY_MULTICAST, confirmed.localDiscoveryMulticastAddress, candidate.localDiscoveryMulticastAddress,
        ),
        announceLanAddresses = appliedFields.select(
            SettingSaveField.ANNOUNCE_LAN, confirmed.announceLanAddresses, candidate.announceLanAddresses,
        ),
        natEnabled = appliedFields.select(SettingSaveField.NAT, confirmed.natEnabled, candidate.natEnabled),
        relaysEnabled = appliedFields.select(SettingSaveField.RELAYS, confirmed.relaysEnabled, candidate.relaysEnabled),
        alwaysLocalNetworks = appliedFields.select(
            SettingSaveField.LAN_SUBNETS, confirmed.alwaysLocalNetworks, candidate.alwaysLocalNetworks,
        ),
        connectionLimitMax = appliedFields.select(
            SettingSaveField.MAX_CONNECTIONS, confirmed.connectionLimitMax, candidate.connectionLimitMax,
        ),
    )
}

private fun <T> Set<SettingSaveField>.select(field: SettingSaveField, confirmed: T, edited: T): T =
    if (field in this) edited else confirmed
