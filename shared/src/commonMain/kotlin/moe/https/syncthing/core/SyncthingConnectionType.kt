package moe.https.syncthing.core

enum class SyncthingConnectionType {
    TCP_LAN,
    TCP_WAN,
    QUIC_LAN,
    QUIC_WAN,
    RELAY_LAN,
    RELAY_WAN,
    DISCONNECTED;

    companion object {
        fun fromCore(type: String?, isLocal: Boolean): SyncthingConnectionType = when {
            type?.startsWith("tcp") == true -> if (isLocal) TCP_LAN else TCP_WAN
            type?.startsWith("quic") == true -> if (isLocal) QUIC_LAN else QUIC_WAN
            type?.startsWith("relay") == true -> if (isLocal) RELAY_LAN else RELAY_WAN
            else -> DISCONNECTED
        }
    }
}
