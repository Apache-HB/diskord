package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.network.Requester
import com.serebit.diskord.network.GatewayAdapter
import com.serebit.diskord.network.endpoints.GetGatewayBot
import com.serebit.diskord.network.payloads.DispatchPayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.reflect.KClass

fun diskord(token: String, init: DiskordBuilder.() -> Unit = {}) = DiskordBuilder(token).apply(init).build()

class DiskordBuilder(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> onEvent(crossinline task: suspend (T) -> Unit) {
        onEvent(T::class) { task(it as T) }
    }

    fun <T : Event> onEvent(eventType: KClass<T>, task: suspend (Event) -> Unit) {
        listeners += EventListener(eventType, task)
    }

    fun build(): Diskord? = runBlocking {
        Requester.token = token
        val response = Requester.requestResponse(GetGatewayBot).await().let {
            if (it.statusCode == HttpURLConnection.HTTP_OK) Serializer.fromJson<GatewayResponse.Valid>(it.text)
            else Serializer.fromJson<GatewayResponse.Invalid>(it.text)
        }

        when (response) {
            is GatewayResponse.Valid -> Diskord(response.url, listeners)
            is GatewayResponse.Invalid -> {
                Logger.error("${response.message}. Failed to connect to Discord.")
                null
            }
        }
    }

    private sealed class GatewayResponse {
        data class Valid(val url: String, val shards: Int) : GatewayResponse()
        data class Invalid(val code: Int, val message: String) : GatewayResponse()
    }
}

class Diskord internal constructor(uri: String, listeners: Set<EventListener>) {
    private val eventDispatcher = EventDispatcher(listeners, ::context)
    private var selfUserId: Long = 0
    private val context by lazy { Context(selfUserId) }
    private val adapter = GatewayAdapter(uri, eventDispatcher) { dispatch: DispatchPayload.Ready ->
        selfUserId = dispatch.d.user.id
    }

    init {
        Runtime.getRuntime().addShutdownHook(thread(false, block = ::exit))
        adapter.openGateway()
    }

    fun exit() = runBlocking {
        adapter.closeGateway()
        // give it a second, the socket closure needs to receive confirmation from Discord. nothing is blocking
        // the thread, so to stop it from ending prematurely, we delay it for a few seconds.
        delay(exitTimeout)
    }

    companion object {
        const val sourceUri = "https://gitlab.com/serebit/diskord"
        const val version = "0.0.0"
        private const val exitTimeout = 5000
    }
}
