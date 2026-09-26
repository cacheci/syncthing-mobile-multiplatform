package moe.https.syncthing.core

import kotlinx.serialization.Serializable

data class SettingConfiguration(
    val deviceName: String,
    val minHomeDiskFree: Double,
    val minHomeDiskFreeUnit: DiskSpaceUnit,
    val usageReportingEnabled: Boolean,
    val usageReportingVersion: Int,
    val guiListenAddress: String,
    val guiPort: Int,
    val guiPortConflictBehavior: GuiPortConflictBehavior,
    val guiAuthenticationEnabled: Boolean,
    val guiUser: String,
    val guiPasswordConfigured: Boolean,
    val newGuiPassword: String = "",
    val guiTheme: GuiTheme,
    val guiUseTls: Boolean,
    val listenAddresses: List<String>,
    val maxSendKiBPerSecond: Int,
    val maxReceiveKiBPerSecond: Int,
    val reconnectionIntervalSeconds: Int,
    val limitBandwidthInLan: Boolean,
    val globalDiscoveryEnabled: Boolean,
    val globalDiscoveryServers: List<String>,
    val localDiscoveryEnabled: Boolean,
    val localDiscoveryPort: Int,
    val localDiscoveryMulticastAddress: String,
    val announceLanAddresses: Boolean,
    val natEnabled: Boolean,
    val relaysEnabled: Boolean,
    val alwaysLocalNetworks: List<String>,
    val connectionLimitEnough: Int,
    val connectionLimitMax: Int,
) {
    enum class DiskSpaceUnit(val apiValue: String, val displayName: String) {
        PERCENT("%", "%"),
        KILOBYTE("kB", "KiB"),
        MEGABYTE("MB", "MiB"),
        GIGABYTE("GB", "GiB"),
        TERABYTE("TB", "TiB"),
    }

    enum class GuiTheme(val apiValue: String) {
        DEFAULT("default"), LIGHT("light"), DARK("dark"), BLACK("black"),
    }

    enum class GuiPortConflictBehavior {
        FAIL, TRY_NEXT,
    }

    @Serializable
    enum class RunningOnPoweredBy {
        CHARGED, BATTERY, BOTH,
    }

    companion object {
        fun startupDefaults(
            guiListenAddress: String = "127.0.0.1",
            guiPort: Int = 8384,
            guiPortConflictBehavior: GuiPortConflictBehavior = GuiPortConflictBehavior.FAIL,
            guiUseTls: Boolean = false,
        ): SettingConfiguration = SettingConfiguration(
            deviceName = "Syncthing",
            minHomeDiskFree = 1.0,
            minHomeDiskFreeUnit = DiskSpaceUnit.PERCENT,
            usageReportingEnabled = false,
            usageReportingVersion = 1,
            guiListenAddress = guiListenAddress,
            guiPort = guiPort,
            guiPortConflictBehavior = guiPortConflictBehavior,
            guiAuthenticationEnabled = false,
            guiUser = "",
            guiPasswordConfigured = false,
            guiTheme = GuiTheme.DEFAULT,
            guiUseTls = guiUseTls,
            listenAddresses = listOf("default"),
            maxSendKiBPerSecond = 0,
            maxReceiveKiBPerSecond = 0,
            reconnectionIntervalSeconds = 60,
            limitBandwidthInLan = false,
            globalDiscoveryEnabled = true,
            globalDiscoveryServers = listOf("default"),
            localDiscoveryEnabled = true,
            localDiscoveryPort = 21027,
            localDiscoveryMulticastAddress = "[ff12::8384]:21027",
            announceLanAddresses = true,
            natEnabled = true,
            relaysEnabled = true,
            alwaysLocalNetworks = emptyList(),
            connectionLimitEnough = 0,
            connectionLimitMax = 0,
        )
    }
}

enum class GuiTlsFile(val fileName: String, val displayName: String) {
    CERTIFICATE("https-cert.pem", "HTTPS 证书"),
    PRIVATE_KEY("https-key.pem", "HTTPS 证书密钥"),
}

enum class SettingAccessMode {
    REST,
    CONFIG_FILE,
    STARTUP_ONLY,
}



data class SettingSnapshot(
    val configuration: SettingConfiguration,
    val accessMode: SettingAccessMode,
)

data class SettingSaveResult(
    val restartRequired: Boolean,
    val accessMode: SettingAccessMode,
    val guiTlsChanged: Boolean = false,
    val restartInitiated: Boolean = false,
)
