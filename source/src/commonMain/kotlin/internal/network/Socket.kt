package com.serebit.strife.internal.network

import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.Payload
import com.serebit.strife.internal.dispatches.Ready
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.KSerializer

internal expect class Socket constructor(uri: String) : CoroutineScope {
    var isOpen: Boolean
        private set

    fun connect()

    fun send(text: String)

    fun <T : Any> send(serializer: KSerializer<T>, obj: T)

    fun onPayload(callback: suspend (Payload) -> Unit)

    fun onHelloPayload(callback: suspend (HelloPayload) -> Unit)

    fun onReadyDispatch(callback: suspend (Ready) -> Unit)

    fun clearListeners()

    fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE)
}
