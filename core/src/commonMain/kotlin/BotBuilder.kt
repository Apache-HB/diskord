package com.serebit.strife

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.strife.BotBuilder.Success.SessionStartLimit
import com.serebit.strife.events.Event
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.network.GatewayRoute
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
    private val listeners = mutableSetOf<EventListener<*>>()
    private val logger = Logger().apply { level = LogLevel.OFF }
    private val sessionInfo = SessionInfo(token, "strife", logger)
    var logToConsole = false
        set(value) {
            logger.level = if (value) LogLevel.TRACE else LogLevel.OFF
            field = value
        }

    /** Add an [EventListener] from a [task] and [type][eventType]. */
    @PublishedApi
    internal fun <T : Event> onEvent(eventType: KClass<T>, task: suspend T.() -> Unit) {
        listeners += EventListener(eventType, task)
    }

    /**
     * Builds the instance. This should only be run after the builder has been fully configured,
     * and will return either an instance of [Context] (if the initial connection succeeds)
     * or null (if the initial connection fails) upon completion.
     */
    suspend fun build(): Context? {
        // Make a request for a gateway connection
        val response = Requester(sessionInfo).use { it.sendRequest(GatewayRoute.GetBot) }

        return if (response.status.isSuccess()) {
            val successPayload = Json.parse(Success.serializer(), response.text)

            logger.debug("Attempting to connect to Discord...")

            Context(successPayload.url, sessionInfo, listeners)
        } else {
            logger.error("${response.version} ${response.status}")
            println("${response.version} ${response.status} ${response.status.errorMessage}")
            null
        }
    }

    private val HttpStatusCode.errorMessage
        get() = when (value) {
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
        /**
         * A data object containing [the total number of session starts the current user is allowed][total],
         * [remaining number of session starts the current user is allowed][remaining], and
         * [the number of milliseconds after which the limit resets][reset_after].
         *
         * [see](https://discordapp.com/developers/docs/topics/gateway#session-start-limit-object)
         */
        @Serializable
        data class SessionStartLimit(val total: Int, val remaining: Int, val reset_after: Long)
    }
}
