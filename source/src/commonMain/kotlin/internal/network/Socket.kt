package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.payloads.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.KSerializer

internal expect class Socket constructor(uri: String) : CoroutineScope {
    var isOpen: Boolean
        private set
    val isClosed: Boolean

    fun connect()

    fun send(text: String)

    fun <T : Any> send(serializer: KSerializer<T>, obj: T)

    fun onPayload(callback: suspend (Payload) -> Unit)

    fun clearListeners()

    fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE)
}
