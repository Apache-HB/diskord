package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.network.ApiEndpoint
import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.GatewayAdapter
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.reflect.KClass

fun diskord(token: String, init: DiskordBuilder.() -> Unit) = DiskordBuilder(token).apply { init() }.build()

class DiskordBuilder internal constructor(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> onEvent(noinline task: suspend (T) -> Unit) =
        addListener(T::class) { task(it as T) }

    fun addListener(eventType: KClass<out Event>, task: suspend (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    internal fun build(): Diskord? = runBlocking {
        ApiRequester.token = token
        val response = ApiRequester.request(ApiEndpoint.gatewayBot).await().let {
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
    private var selfUserId: Long? = null
    private val context get() = Context(selfUserId!!)
    private val adapter = GatewayAdapter(uri, eventDispatcher) { dispatch: Payload.Dispatch.Ready ->
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
        delay(5000)
    }

    companion object {
        const val version = "0.0.0"
    }
}
