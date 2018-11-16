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
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * The builder class for the main [Bot] class. This class can be used manually in classic Java fashion, but it is
 * recommended that developers use the [bot] method instead.
 */
class BotBuilder(private val token: String) {
    private val listeners: MutableSet<EventListener> = mutableSetOf()

    /**
     * Creates an event listener for events with type T. The code inside the [task] block will be executed every time
     * the bot receives an event with type T.
     */
    inline fun <reified T : Event> onEvent(crossinline task: suspend (T) -> Unit) = onEvent(T::class) { task(it as T) }

    /**
     * Creates an event listener for events with type [eventType]. The code inside the [task] block will be executed
     * every time the bot receives an event with the given type.
     */
    fun <T : Event> onEvent(eventType: KClass<T>, task: suspend (Event) -> Unit) =
        listeners.add(EventListener(eventType, task))

    /**
     * Builds the instance. This should only be run after the builder has been fully configured, and will return
     * either an instance of [Bot] (if the initial connection succeeds) or null (if the initial connection fails)
     * upon completion.
     */
    fun build(): Bot? = runBlocking {
        Requester.initialize(token)
        val response = Requester.requestResponse(GetGatewayBot)

        if (response.status.isSuccess()) {
            val responseText = response.content.readRemaining().readText()
            Bot(
                JSON.parse(Success.serializer(), responseText).url,
                token, listeners
            )
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

    @Serializable
    private data class Success(val url: String)
}

/**
 * Creates a new instance of the Bot base class. This is the recommended method of initializing a Discord bot
 * using this library.
 *
 * @param token The Discord-provided token used to connect to Discord's servers. A token can be obtained from
 * https://discordapp.com/developers/applications/me.
 *
 * @param init The initialization block. Event listeners should be declared here using the provided methods in
 * [BotBuilder].
 */
fun bot(token: String, init: BotBuilder.() -> Unit = {}) = BotBuilder(token).apply(init).build()
