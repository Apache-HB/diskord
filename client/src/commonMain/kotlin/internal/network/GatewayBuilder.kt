package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.DispatchPayload
import kotlinx.coroutines.CoroutineScope

private typealias DispatchListener = suspend (CoroutineScope, DispatchPayload) -> Unit

internal fun buildGateway(
    uri: String,
    token: String,
    logger: Logger,
    init: GatewayBuilder.() -> Unit
): Gateway = GatewayBuilder(uri, token, logger).apply(init).build()

internal class GatewayBuilder(private val uri: String, private val token: String, private val logger: Logger) {
    private val dispatchListeners = mutableListOf<DispatchListener>()

    fun onDispatch(task: DispatchListener) {
        dispatchListeners += task
    }

    fun build() = Gateway(uri, token, logger, GatewayListener(dispatchListeners.toList()))
}

internal data class GatewayListener(private val dispatchListeners: List<DispatchListener>) {
    suspend fun onDispatch(scope: CoroutineScope, dispatch: DispatchPayload) = dispatchListeners.forEach {
        it(scope, dispatch)
    }
}
