package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.network.Gateway
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetGatewayBot
import com.serebit.loggerkt.Logger
import org.http4k.core.Status
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess

fun diskord(token: String, init: DiskordBuilder.() -> Unit = {}) = DiskordBuilder(token).apply(init).build()

class DiskordBuilder(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> onEvent(crossinline task: suspend (T) -> Unit) = onEvent(T::class) { task(it as T) }

    fun <T : Event> onEvent(eventType: KClass<T>, task: suspend (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    fun build(): Diskord? {
        Requester.initialize(token)
        val response = Requester.requestResponse(GetGatewayBot)

        return if (response.status.successful) {
            Diskord(JSON.parse<Success>(response.bodyString()).url, token, listeners)
        } else {
            Logger.error("${response.version} ${response.status}")
            println(response.status.errorMessage)
            null
        }
    }

    private val Status.errorMessage get() = when (this) {
        Status.UNAUTHORIZED -> "Discord refused to connect. Make sure your token is valid."
        Status.SERVICE_UNAVAILABLE -> "Discord's servers are down. Try again later."
        Status.UNKNOWN_HOST -> "Couldn't resolve the host. Are you connected to the Internet?"
        Status.BAD_REQUEST -> "Something was wrong with the data sent to Discord. File a bug report."
        Status.NOT_FOUND -> "The authentication page doesn't exist. File a bug report."
        Status.I_M_A_TEAPOT -> "Discord is a teapot, apparently. Not sure what's going on there."
        else -> "Failed to connect to Discord, with an HTTP error code of $code."
    }

    private data class Success(val url: String)
}

class Diskord internal constructor(uri: String, token: String, listeners: Set<EventListener>) {
    private val eventDispatcher = EventDispatcher(listeners)
    private val gateway = Gateway(uri, token, eventDispatcher)

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
