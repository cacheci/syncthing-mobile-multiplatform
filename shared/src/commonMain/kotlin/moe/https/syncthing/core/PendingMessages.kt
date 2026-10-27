package moe.https.syncthing.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Instance identity distinguishes separate events with identical content.
class PendingMessage<T>(val value: T)

class PendingMessages<T> {
    private val mutableMessages = MutableStateFlow<List<PendingMessage<T>>>(emptyList())
    val messages = mutableMessages.asStateFlow()

    fun add(value: T): PendingMessage<T> {
        val message = PendingMessage(value)
        mutableMessages.update { it + message }
        return message
    }

    fun consume(message: PendingMessage<T>): Boolean {
        while (true) {
            val current = mutableMessages.value
            if (message !in current) return false
            if (mutableMessages.compareAndSet(current, current - message)) return true
        }
    }
}
