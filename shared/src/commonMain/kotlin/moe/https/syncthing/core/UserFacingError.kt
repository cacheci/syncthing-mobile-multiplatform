package moe.https.syncthing.core

/** Supplies localized UI text while exception.message remains available for diagnostics. */
interface UserFacingError {
    suspend fun localizedUserMessage(): String
}

suspend fun Throwable.localizedUserMessageOrNull(): String? {
    val userFacingError = generateSequence(this) { it.cause }
        .filterIsInstance<UserFacingError>()
        .firstOrNull()
    return userFacingError?.localizedUserMessage()
        ?: message?.takeIf(String::isNotBlank)
}
