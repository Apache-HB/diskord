package com.serebit.strife

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.strife.BotBuilder.Success.SessionStartLimit
import com.serebit.strife.events.Event
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.EventResult
import com.serebit.strife.internal.IndefiniteEventListener
import com.serebit.strife.internal.TerminableEventListener
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.io.core.use
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlin.reflect.KType

/**
 * The builder class for the main [BotClient] class. This class can be used manually in classic
 * Java fashion, but it is recommended that developers use the [bot] method instead.
 */
class BotBuilder(private val token: String) {
    private val listeners = mutableSetOf<EventListener<*>>()
    private var logLevel = LogLevel.OFF
    private val _features = mutableMapOf<String, BotFeature>()
    /** Installed [bot features][BotFeature] mapped {[name][BotFeature.name] -> [BotFeature]}. */
    val features: Map<String, BotFeature> get() = _features.toMap()
    /** Set this to `true` to print the internal logger to the console. */
    var logToConsole: Boolean = false
        set(value) {
            logLevel = if (value) LogLevel.TRACE else LogLevel.OFF
            field = value
        }

    /** Attaches the specified feature to this bot, via its [provider]. */
    fun <TFeature : BotFeature> install(provider: BotFeatureProvider<TFeature>) {
        val feature = provider.provide()
        feature.installTo(this)
        _features[feature.name] = feature
    }

    /**
     * Returns the installed [BotFeature] of the given [type][TFeature]
     * or `null` if no feature of the type is installed.
     */
    inline fun <reified TFeature : BotFeature> getFeature(): TFeature? =
        features.values.firstOrNull { it is TFeature } as? TFeature

    @PublishedApi
    internal fun <T : Event> onEvent(eventType: KType, task: suspend T.() -> Unit) {
        listeners += IndefiniteEventListener(eventType, task)
    }

    /**
     * Creates a new [TerminableEventListener] of type [T] and adds it to the [listeners] set.
     * @param runLimit The number of successful runs before the listener is terminated.
     * @param task The task to run.
     */
    @PublishedApi
    internal fun <T : Event> onTerminableEvent(eventType: KType, runLimit: Int, task: suspend T.() -> EventResult) {
        listeners += TerminableEventListener(eventType, runLimit, task)
    }

    /**
     * Builds the instance. This should only be run after the builder has been fully configured,
     * and will return either an instance of [BotClient] (if the initial connection succeeds)
     * or null (if the initial connection fails) upon completion.
     */
    @UseExperimental(UnstableDefault::class)
    suspend fun build(): BotClient? {

        val tLog = Logger().apply { level = logLevel }

        tLog.debug("Attempting to connect to Discord...")
        val success: Success = Requester(token, tLog)
            .use { it.sendRequest(Route.GetGatewayBot) }
            .run {
                if (status.isSuccess() && text != null) {
                    Json.parse(Success.serializer(), text)
                } else {
                    tLog.error("Failed to get gateway information. $version $status")
                    println("$version $status ${status.errorMessage}")
                    return null
                }
            }

        return buildClient(success.url)
    }

    /**
     * Make a request for a gateway connection.
     */
    @UseExperimental(UnstableDefault::class)
    private fun buildClient(url: String): BotClient =
        BotClient(url, token, Logger().apply { level = logLevel }, listeners)

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
