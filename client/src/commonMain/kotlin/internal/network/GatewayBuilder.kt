package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.DispatchPayload

internal fun buildGateway(uri: String, token: String, logger: Logger, init: GatewayBuilder.() -> Unit): Gateway =
    GatewayBuilder(uri, token, logger).apply(init).build()

internal class GatewayBuilder(private val uri: String, private val token: String, private val logger: Logger) {
    private val dispatchListeners = mutableListOf<suspend (DispatchPayload) -> Unit>()

    fun onDispatch(task: suspend (DispatchPayload) -> Unit) {
        dispatchListeners += task
    }

    fun build() = Gateway(uri, token, logger, GatewayListener(dispatchListeners.toList()))
}

internal data class GatewayListener(private val dispatchListeners: List<suspend (DispatchPayload) -> Unit>) {
    suspend fun onDispatch(dispatch: DispatchPayload) =
        dispatchListeners.forEach { it(dispatch)}
}
