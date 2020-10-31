package com.serebit.strife

import com.serebit.logkat.LogLevel
import com.serebit.logkat.Logger
import com.serebit.logkat.error
import com.serebit.strife.Success.SessionStartLimit
import com.serebit.strife.events.Event
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.stackTraceAsString
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

/**
 * The builder class for the main [BotClient] class. This class can be used manually in classic Java fashion, but it is
 * recommended that developers use the [bot] method instead.
 */
class BotBuilder(private val token: String) {
    private var logLevel = LogLevel.OFF

    private val _addons = mutableMapOf<String, BotAddon>()

    private val eventListeners = mutableListOf<suspend (Event) -> Unit>()

    /** Installed [bot addons][BotAddon] mapped {[name][BotAddon.name] -> [BotAddon]}. */
    val addons: Map<String, BotAddon> get() = _addons.toMap()

    /** Set this to `true` to print the internal logger to the console. */
    var logToConsole: Boolean = false
        set(value) {
            logLevel = if (value) LogLevel.TRACE else LogLevel.OFF
            field = value
        }

    /** Attaches the specified addon to this bot, via its [provider]. */
    suspend fun <A : BotAddon> install(provider: BotAddonProvider<A>, config: suspend A.() -> Unit = {}) {
        val addon = provider.provide()
        config(addon)
        addon.installTo(this)
        _addons[addon.name] = addon
    }

    @PublishedApi
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    internal fun addEventListener(task: suspend (Event) -> Unit) {
        eventListeners += task
    }

    /**
     * Builds the instance. This should only be run after the builder has been fully configured, and will return a valid
     * instance of [BotClient].
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    suspend fun build(): BotClient? {
        val logger = Logger().apply { level = logLevel }
        val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            logger.error("Exception in event listener: ${exception.stackTraceAsString}")
        }

        val eventDispatcher = BroadcastChannel<Event>(BUFFERED)
        eventListeners.forEach { listener ->
            eventDispatcher.asFlow()
                .onEach { coroutineScope.launch(exceptionHandler) { listener(it) } }
                .launchIn(coroutineScope)
        }

        return getSuccessPayload(logger)?.let { BotClient(it.url, token, coroutineScope, logger, eventDispatcher) }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun getSuccessPayload(logger: Logger): Success? {
        val tempRequester = Requester(token, logger)
        val success = tempRequester.sendRequest(Route.GetGatewayBot).run {
            if (status.isSuccess() && text != null) {
                Json.decodeFromString(Success.serializer(), text)
            } else {
                logger.error("Failed to get gateway information. $version $status ${status.errorMessage}")
                null
            }
        }

        tempRequester.close()

        return success?.let {
            if (it.session_start_limit.remaining == 0) {
                val resetTime = it.session_start_limit.remaining.milliseconds
                logger.error("Session start limit reached for this token. Limit resets in $resetTime.")
                null
            } else success
        }
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
    data class SessionStartLimit(val total: Int, val remaining: Int, val reset_after: Long, val max_concurrency: Int)
}
