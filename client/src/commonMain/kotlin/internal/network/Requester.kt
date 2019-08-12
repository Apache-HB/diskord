package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.stackTraceAsString
import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.io.core.Closeable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * An internal object for making REST requests to the Discord API.
 *
 * @property sessionInfo The [SessionInfo] instance used to authorise REST requests.
 * This is also where the [logger] reference is taken from.
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
internal class Requester(private val sessionInfo: SessionInfo) : Closeable {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    /** The [Requester]'s [HttpClient]. */
    private val handler = HttpClient()
    /** The [Logger] of the [sessionInfo]. */
    private val logger = sessionInfo.logger
    private val routeChannels = mutableMapOf<String, SendChannel<Request>>()
    private var globalBroadcast: BroadcastChannel<Unit>? = null
    @UseExperimental(UnstableDefault::class)
    private val serializer = Json {
        strictMode = false
        serialModule = ChannelPacket.serializerModule
    }

    @UseExperimental(UnstableDefault::class)
    suspend fun <R : Any> sendRequest(route: Route<R>): Response<R> {
        logger.trace("Requesting object from endpoint $route")

        val response = requestHttpResponse(route)

        val responseText = try {
            response.readText()
        } catch (ex: ClientRequestException) {
            logger.error("Error in requester: ${ex.stackTraceAsString}")
            null
        } finally {
            response.close()
        }

        val responseData = responseText
            ?.takeUnless { it.isBlank() }
            ?.also { text ->
                text
                    .takeUnless { response.status.isSuccess() }
                    ?.let { Json.nonstrict.parseJson(text) as? JsonObject }
                    ?.let { it["code"] }
                    ?.also { logger.error("Request from route $route failed with JSON error code $it") }
            }?.let { text ->
                route.serializer?.let { serializer.parse(it, text) }
            }

        return Response(response.status, response.version, responseText, responseData)
    }

    @UseExperimental(FlowPreview::class)
    private suspend fun <R : Any> requestHttpResponse(endpoint: Route<R>) = Request(endpoint).let { request ->
        routeChannels.getOrPut(endpoint.ratelimitPath) {
            Channel<Request>().also { channel ->
                coroutineScope.launch {
                    channel.consumeAsFlow().collect {
                        withTimeout(10_000) { makeRequest(it) }
                    }

                    routeChannels.remove(endpoint.ratelimitPath)
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

            response = handler.call(request.endpoint.uri) {
                method = request.endpoint.method
                headers.appendAll(sessionInfo.defaultHeaders)
                request.endpoint.requestPayload.parameters.map { parameter(it.key, it.value) }
                request.endpoint.requestPayload.body?.let { body = it }
            }.response

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
            ?.let { it * 1000 - DateTime.parse(headers["date"].toString()).utc.unixMillisLong }

    override fun close() {
        coroutineScope.cancel()
        handler.close()
    }
}

internal data class RequestPayload(
    val parameters: Map<String, String> = emptyMap(),
    val body: TextContent? = null
)

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
