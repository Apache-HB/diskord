package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.network.Gateway
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetGatewayBot
import com.serebit.logkat.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.experimental.io.readRemaining
import kotlinx.coroutines.experimental.runBlocking
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess

fun diskord(token: String, init: DiskordBuilder.() -> Unit = {}) = DiskordBuilder(token).apply(init).build()

class DiskordBuilder(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    inline fun <reified T : Event> onEvent(crossinline task: suspend (T) -> Unit) = onEvent(T::class) { task(it as T) }

    fun <T : Event> onEvent(eventType: KClass<T>, task: suspend (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    fun build(): Diskord? = runBlocking {
        Requester.initialize(token)
        val response = Requester.requestResponse(GetGatewayBot)

        if (response.status.isSuccess()) {
            Diskord(JSON.parse<Success>(response.content.readRemaining().readText()).url, token, listeners)
        } else {
            Logger.error("${response.version} ${response.status}")
            println(response.status.errorMessage)
            null
        }
    }

    private val HttpStatusCode.errorMessage
        get() = when (this) {
            HttpStatusCode.Unauthorized -> "Discord refused to connect. Make sure your token is valid."
            HttpStatusCode.ServiceUnavailable -> "Discord's servers are down. Try again later."
//            HttpStatusCode. -> "Couldn't resolve the host. Are you connected to the Internet?"
            HttpStatusCode.BadRequest -> "Something was wrong with the data sent to Discord. File a bug report."
            HttpStatusCode.NotFound -> "The authentication page doesn't exist. File a bug report."
            else -> "Failed to connect to Discord, with an HTTP error code of $value."
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


    fun exit() {
        gateway.disconnect()
        println("Disconnected from Discord.")
    }

    companion object {
        const val sourceUri = "https://gitlab.com/serebit/diskord"
        const val version = "0.0.0"
    }
}
