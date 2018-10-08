package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.payloads.Payload
import kotlinx.coroutines.CoroutineScope

internal expect class Socket constructor(uri: String) : CoroutineScope {
    var isOpen: Boolean
        private set
    val isClosed: Boolean

    fun connect()

    fun send(text: String)

    fun send(obj: Any)

    fun onPayload(callback: suspend (Payload) -> Unit)

    fun clearListeners()

    fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE)
}
