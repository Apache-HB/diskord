package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetGatewayBot
import com.serebit.diskord.internal.runBlocking
import com.serebit.logkat.Logger
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.io.readRemaining
import kotlin.reflect.KClass

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
            HttpStatusCode.BadRequest -> "Something was wrong with the data sent to Discord. File a bug report."
            HttpStatusCode.NotFound -> "The authentication page doesn't exist. File a bug report."
            else -> "Failed to connect to Discord, with an HTTP error code of $value."
        }

    private data class Success(val url: String)
}

fun diskord(token: String, init: DiskordBuilder.() -> Unit = {}) = DiskordBuilder(token).apply(init).build()
