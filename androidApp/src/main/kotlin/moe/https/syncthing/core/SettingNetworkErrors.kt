package moe.https.syncthing.core

import java.io.EOFException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

internal fun isSettingNetworkError(error: Throwable): Boolean = when (error) {
    is SSLException, is SyncthingRestException -> false
    is SocketException, is SocketTimeoutException, is UnknownHostException, is EOFException -> true
    else -> false
}

internal fun <T> withSettingNetworkErrors(request: () -> T): T = try {
    request()
} catch (error: Exception) {
    if (isSettingNetworkError(error)) throw SettingNetworkException(error)
    throw error
}
