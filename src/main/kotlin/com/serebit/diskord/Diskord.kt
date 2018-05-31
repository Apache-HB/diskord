package com.serebit.diskord

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.GatewayAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.reflect.KClass

fun diskord(token: String, init: DiskordBuilder.() -> Unit) = DiskordBuilder(token).apply(init).build()

class DiskordBuilder internal constructor(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> listener(crossinline task: (T) -> Unit) =
        addListener(T::class) { task(it as T) }

    fun addListener(eventType: KClass<out Event>, task: (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    internal fun build() = Diskord(token, listeners)
}

class Diskord internal constructor(token: String, listeners: Set<EventListener>) {
    private val eventDispatcher = EventDispatcher(listeners)

    init {
        ApiRequester.token = token

        val response = ApiRequester.get("/gateway/bot").let {
            Gson().fromJson<GatewayResponse>(it.text)
        }

        OkHttpClient().newWebSocket(
            Request.Builder().url(response.url).build(),
            GatewayAdapter(eventDispatcher)
        )
    }

    private data class GatewayResponse(val url: String, val shards: Int)
}
