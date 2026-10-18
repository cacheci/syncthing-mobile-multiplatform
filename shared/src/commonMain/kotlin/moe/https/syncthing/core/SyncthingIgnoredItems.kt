package moe.https.syncthing.core

data class SyncthingIgnoredDevice(
    val id: String,
    val name: String?,
    val address: String?,
)

data class SyncthingIgnoredFolder(
    val id: String,
    val label: String?,
    val deviceId: String,
    val deviceName: String?,
)

data class IgnoredItemsSnapshot(
    val devices: List<SyncthingIgnoredDevice>,
    val folders: List<SyncthingIgnoredFolder>,
)
