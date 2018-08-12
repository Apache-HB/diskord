package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.events.EventListener
import com.serebit.diskord.network.Gateway
import com.serebit.diskord.network.Requester
import com.serebit.diskord.network.endpoints.GetGatewayBot
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess

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
        Requester.initialize(token)
        val response = Requester.requestResponse(GetGatewayBot).await().let {
            if (it.statusCode == HttpURLConnection.HTTP_OK) Serializer.fromJson<GatewayResponse.Valid>(it.text)
            else Serializer.fromJson<GatewayResponse.Invalid>(it.text)
        }

        when (response) {
            is GatewayResponse.Valid -> Diskord(response.url, token, listeners)
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

class Diskord internal constructor(uri: String, token: String, listeners: Set<EventListener>) {
    private val context = Context(token)
    private val eventDispatcher = EventDispatcher(listeners, context)
    private val gateway = Gateway(uri, eventDispatcher)

    init {
        Logger.debug("Attempting to connect to Discord...")
        val hello = gateway.connect() ?: run {
            Logger.fatal("Failed to connect to Discord.")
            exitProcess(0)
        }
        Logger.debug("Connected and received Hello payload. Opening session...")
        gateway.openSession(hello)?.let {
            println("Connected to Discord.")
        }
        Runtime.getRuntime().addShutdownHook(thread(false, block = ::exit))
    }


    fun exit() = gateway.disconnect()

    companion object {
        const val sourceUri = "https://gitlab.com/serebit/diskord"
        const val version = "0.0.0"
    }
}
