package moe.https.syncthing.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/** Marks a transport failure without exposing platform exception types to shared code. */
class SettingNetworkException(cause: Throwable) : Exception(cause.message, cause)

internal suspend fun <T> retrySettingSave(
    waitBeforeRetry: suspend (Long) -> Unit = { delay(it) },
    save: suspend () -> T,
): T {
    var retries = 0
    while (true) {
        currentCoroutineContext().ensureActive()
        try {
            return save()
        } catch (error: CancellationException) {
            throw error
        } catch (error: SettingNetworkException) {
            if (retries >= MAX_SETTING_SAVE_RETRIES) throw error
            retries++
            waitBeforeRetry(SETTING_SAVE_RETRY_DELAY_MILLIS)
        }
    }
}

private const val MAX_SETTING_SAVE_RETRIES = 3
private const val SETTING_SAVE_RETRY_DELAY_MILLIS = 2_000L
