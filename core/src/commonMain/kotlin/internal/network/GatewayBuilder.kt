package com.serebit.strife.internal.network

import com.serebit.strife.internal.DispatchPayload
import kotlinx.coroutines.CoroutineScope

private typealias DispatchListener = suspend (CoroutineScope, DispatchPayload) -> Unit

internal fun buildGateway(uri: String, sessionInfo: SessionInfo, init: GatewayBuilder.() -> Unit) =
    GatewayBuilder(uri, sessionInfo).apply(init).build()

internal class GatewayBuilder(private val uri: String, private val sessionInfo: SessionInfo) {
    private val dispatchListeners = mutableListOf<DispatchListener>()

    fun onDispatch(task: DispatchListener) {
        dispatchListeners += task
    }

    fun build() = Gateway(uri, sessionInfo, GatewayListener(dispatchListeners.toList()))
}

internal data class GatewayListener(
    private val dispatchListeners: List<DispatchListener>
) {
    suspend fun onDispatch(scope: CoroutineScope, dispatch: DispatchPayload) = dispatchListeners.forEach {
        it(scope, dispatch)
    }
}
