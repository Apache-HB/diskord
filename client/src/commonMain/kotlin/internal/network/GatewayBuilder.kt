package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.DispatchPayload
import kotlinx.coroutines.CoroutineScope

private typealias DispatchListener = suspend (CoroutineScope, DispatchPayload) -> Unit

internal fun buildGateway(
    uri: String,
    token: String,
    shardID: Int,
    shardCount: Int,
    logger: Logger,
    init: GatewayBuilder.() -> Unit
): Gateway = GatewayBuilder(uri, token, shardID, shardCount, logger).apply(init).build()

internal class GatewayBuilder(
    private val uri: String,
    private val token: String,
    private val shardID: Int,
    private val shardCount: Int,
    private val logger: Logger
) {
    private val dispatchListeners = mutableListOf<DispatchListener>()

    fun onDispatch(task: DispatchListener) {
        dispatchListeners += task
    }

    fun build() = Gateway(uri, token, shardID, shardCount, logger, GatewayListener(dispatchListeners.toList()))
}

internal data class GatewayListener(private val dispatchListeners: List<DispatchListener>) {
    suspend fun onDispatch(scope: CoroutineScope, dispatch: DispatchPayload) = dispatchListeners.forEach {
        it(scope, dispatch)
    }
}
