package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.GatewayAdapter
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
            Serializer.fromJson<GatewayResponse>(it.text)
        }

        GatewayAdapter(response.url, eventDispatcher)
    }

    private data class GatewayResponse(val url: String, val shards: Int)
}
