package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.GatewayAdapter
import com.serebit.loggerkt.Logger
import java.net.HttpURLConnection
import kotlin.reflect.KClass

fun diskord(token: String, init: DiskordBuilder.() -> Unit) = DiskordBuilder(token).apply(init).build()

class DiskordBuilder internal constructor(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> onEvent(crossinline task: (T) -> Unit) =
        addListener(T::class) { task(it as T) }

    fun addListener(eventType: KClass<out Event>, task: (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    internal fun build(): Diskord? {
        ApiRequester.token = token
        val response = ApiRequester.get("/gateway/bot").let {
            if (it.statusCode == HttpURLConnection.HTTP_OK) Serializer.fromJson<GatewayResponse.Valid>(it.text)
            else Serializer.fromJson<GatewayResponse.Invalid>(it.text)
        }

        return when (response) {
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
        adapter.openSocket(false)
    }
}
