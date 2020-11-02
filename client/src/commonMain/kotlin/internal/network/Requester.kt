package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.logkat.error
import com.serebit.logkat.trace
import com.serebit.strife.StrifeInfo
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.stackTraceAsString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

/**
 * An internal object for making REST requests to the Discord API. This will attach the given bot token to all
 * requests for authorization purposes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class Requester(token: String, private val logger: Logger) : Closeable {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val handler = HttpClient()
    private val routeChannels = mutableMapOf<String, SendChannel<Request>>()
    private var globalBroadcast: BroadcastChannel<Unit>? = null

    private val serializer = Json {
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = ChannelPacket.serializerModule
        classDiscriminator = "type"
    }
    private val defaultHeaders = headersOf(
        "User-Agent" to listOf("DiscordBot (${StrifeInfo.sourceUri}, ${StrifeInfo.version})"),
        "Authorization" to listOf("Bot $token")
    )

    suspend fun <R : Any> sendRequest(route: Route<R>): Response<R> {
        logger.trace("Requesting object from endpoint $route")

        val response = requestHttpResponse(route)

        val responseText = try {
            response.readText()
        } catch (ex: ClientRequestException) {
            logger.error("Error in requester: ${ex.stackTraceAsString}")
            null
        }

        val responseData = responseText
            ?.takeUnless { it.isBlank() }
            ?.also { text ->
                text
                    .takeUnless { response.status.isSuccess() }
                    ?.let { serializer.encodeToJsonElement(text) as? JsonObject }
                    ?.let { it["code"] }
                    ?.also { logger.error("Request from route $route failed with JSON error code $it") }
            }?.let { text ->
                route.serializer?.let { serializer.decodeFromString(it, text) }
            }

        return Response(response.status, response.version, responseText, responseData)
    }

    @OptIn(FlowPreview::class)
    private suspend fun <R : Any> requestHttpResponse(endpoint: Route<R>) = Request(endpoint).let { request ->
        routeChannels.getOrPut(endpoint.ratelimitKey) {
            Channel<Request>().also { channel ->
                coroutineScope.launch {
                    channel.consumeAsFlow().collect {
                        withTimeout(10_000) { makeRequest(it) }
                    }

                    routeChannels.remove(endpoint.ratelimitKey)
                    channel.close()
                }
            }
        }.send(request)

        request.deferred.await()
    }

    private suspend fun makeRequest(request: Request) {
        var response: HttpResponse

        do {
            globalBroadcast?.openSubscription()?.receive()

            response = handler.request(request.endpoint.uri) {
                method = request.endpoint.method
                headers.appendAll(defaultHeaders)
                body = request.endpoint.body
                request.endpoint.parameters.map { parameter(it.key, it.value) }
            }

            if (response.status.value == HttpStatusCode.TooManyRequests.value) {
                logger.error("Encountered 429 with route ${request.endpoint.uri}")

                val broadcast = response.headers["x-ratelimit-global"]
                    ?.takeIf { globalBroadcast == null }
                    ?.let { BroadcastChannel<Unit>(1) }
                    ?.also { globalBroadcast = it }

                response.resetDelay?.also { delay(it) }

                broadcast?.also {
                    globalBroadcast = null
                    it.send(Unit)
                    it.close()
                }
            }
        } while (response.status.value == HttpStatusCode.TooManyRequests.value)

        request.deferred.complete(response)

        if (response.headers["x-ratelimit-remaining"] == "0") {
            response.resetDelay?.also { delay(it) }
        }
    }

    private inline val HttpResponse.resetDelay
        get() = headers["x-ratelimit-reset"]?.toLongOrNull()
            ?.let { it * 1000 - Clock.System.now().toEpochMilliseconds() }

    override fun close() {
        coroutineScope.cancel()
        handler.close()
    }
}

/** An object to hold the [typed][T] response to a REST request made by a [Requester]. */
internal data class Response<T>(
    val status: HttpStatusCode,
    val version: HttpProtocolVersion,
    val text: String?,
    val value: T?
)

internal data class Request(
    val endpoint: Route<out Any>,
    val deferred: CompletableDeferred<HttpResponse> = CompletableDeferred()
)
