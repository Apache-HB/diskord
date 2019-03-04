package com.serebit.strife

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.strife.BotBuilder.Success.SessionStartLimit
import com.serebit.strife.events.Event
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.ReadyEvent
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.eventListener
import com.serebit.strife.internal.network.Endpoint.GetGatewayBot
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.io.core.use
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * The builder class for the main [Context] class. This class can be used manually in classic
 * Java fashion, but it is recommended that developers use the [bot] method instead.
 */
class BotBuilder(token: String) {
    private val listeners = mutableSetOf<EventListener>()
    private val logger = Logger().apply { level = LogLevel.OFF }
    private val sessionInfo = SessionInfo(token, "strife", logger)
    var logToConsole = false
        set(value) {
            logger.level = if (value) LogLevel.TRACE else LogLevel.OFF
            field = value
        }

    /**
     * Builds the instance. This should only be run after the builder has been fully configured,
     * and will return either an instance of [Context] (if the initial connection succeeds)
     * or null (if the initial connection fails) upon completion.
     */
    suspend fun build(): Context? {
        // Make a request for a gateway connection
        val response = Requester(sessionInfo).use { it.sendRequest(GetGatewayBot) }

        return if (response.status.isSuccess()) {
            val successPayload = Json.parse(Success.serializer(), response.text)

            logger.debug("Attempting to connect to Discord...")

            val gateway = Gateway(successPayload.url, sessionInfo)
            gateway.connect()?.let { hello ->
                Context(hello, gateway, sessionInfo, listeners)
            } ?: null.also { logger.error("Failed to connect to Discord via websocket.") }
        } else {
            logger.error("${response.version} ${response.status}")
            println("${response.version} ${response.status} ${response.status.errorMessage}")
            null
        }
    }

    /**
     * Creates an event listener for events with type T. The code inside the [task] block
     * will be executed every time the bot receives an event with type T.
     */
    inline fun <reified T : Event> onEvent(noinline task: suspend T.() -> Unit) = onEvent(T::class, task)

    /**
     * Creates an event listener for events with type [eventType]. The code inside the
     * [task] block will be executed every time the bot receives an event with the given type.
     */
    @PublishedApi
    internal fun <T : Event> onEvent(eventType: KClass<T>, task: suspend T.() -> Unit) {
        listeners += eventListener(eventType, task)
    }

    /** Convenience method to create an event listener that will execute on reception of a ReadyEvent. */
    fun onReady(task: suspend ReadyEvent.() -> Unit) = onEvent(task)

    /** Convenience method to create an event listener that will execute when a message is created. */
    fun onMessage(task: suspend MessageCreatedEvent.() -> Unit) = onEvent(task)

    private val HttpStatusCode.errorMessage
        get() = when (this.value) {
            HttpStatusCode.Unauthorized.value -> "Discord refused to connect. Make sure your token is valid."
            HttpStatusCode.ServiceUnavailable.value -> "Discord's servers are down. Try again later."
            HttpStatusCode.BadRequest.value -> "Something was wrong with the data sent to Discord. File a bug report."
            HttpStatusCode.NotFound.value -> "The authentication page doesn't exist. File a bug report."
            else -> "Failed to connect to Discord, with an HTTP error code of $value."
        }

    /**
     * Gateway connection success response with metadata regarding [websocket connection][url],
     * the recommended [shard count][shards], [total allowed session starts][SessionStartLimit.total],
     * [remaining allowed session starts][SessionStartLimit.remaining], and
     * [number of milliseconds after which the limit resets][SessionStartLimit.reset_after].
     *
     * [see](https://discordapp.com/developers/docs/topics/gateway#get-gateway-bot)
     */
    @Serializable
    private data class Success(val url: String, val shards: Int, val session_start_limit: SessionStartLimit) {
        @Serializable
        data class SessionStartLimit(val total: Int, val remaining: Int, val reset_after: Long)
    }
}

/**
 * Creates a new instance of the [Context] base class. This is the recommended method of
 * initializing a Discord bot using this library.
 *
 * @param token The Discord-provided token used to connect to Discord's servers. A token
 * can be obtained from [the applications page](https://discordapp.com/developers/applications/me).
 * @param init The initialization block. Event listeners should be declared here using the
 * provided methods in [BotBuilder].
 */
suspend inline fun bot(token: String, init: BotBuilder.() -> Unit = {}) {
    BotBuilder(token).apply(init).build()?.connect()
}
