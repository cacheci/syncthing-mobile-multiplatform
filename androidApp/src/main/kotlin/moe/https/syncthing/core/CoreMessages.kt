package moe.https.syncthing.core

import java.io.IOException

internal open class LocalizedIOException(
    private val userMessage: CoreMessage,
    cause: Throwable? = null,
) : IOException(userMessage.diagnostic(), cause), UserFacingError {
    override suspend fun localizedUserMessage(): String = userMessage.localized()
}

internal class LocalizedIllegalArgumentException(
    private val userMessage: CoreMessage,
) : IllegalArgumentException(userMessage.diagnostic()), UserFacingError {
    override suspend fun localizedUserMessage(): String = userMessage.localized()
}

internal class LocalizedIllegalStateException(
    private val userMessage: CoreMessage,
) : IllegalStateException(userMessage.diagnostic()), UserFacingError {
    override suspend fun localizedUserMessage(): String = userMessage.localized()
}
