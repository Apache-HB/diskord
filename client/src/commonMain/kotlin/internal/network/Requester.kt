package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.logkat.error
import com.serebit.logkat.trace
import com.serebit.strife.StrifeInfo
import com.serebit.strife.internal.newSingleThreadContext
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.stackTraceAsString
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.TooManyRequests
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * An internal object for making REST requests to the Discord API. This will attach the given bot token to all
 * requests for authorization purposes.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class Requester(token: String, private val logger: Logger) : Closeable {
    private val context = newSingleThreadContext("Requester")
    private val handler = HttpClient()

    private val routeBucketsMap = mutableMapOf<String, Deferred<String>>()
    private val ratelimitsMap = mutableMapOf<String, Mutex>()
    private var globalRatelimitJob: Job? = null

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
        val requestBuilder = HttpRequestBuilder().apply {
            method = route.method
            url(route.uri)
            headers.appendAll(defaultHeaders)
            body = route.body
            route.parameters.map { parameter(it.key, it.value) }
        }
        var response: HttpResponse

        withContext(context) {
            var mutex = routeBucketsMap[route.ratelimitKey]?.await()
                ?.let { ratelimitsMap.getOrPut(formatRatelimitID(route.majorParameter, it)) { Mutex() } }
            val deferred = takeIf { mutex == null }
                ?.let { CompletableDeferred<String>() }
                ?.also { routeBucketsMap[route.ratelimitKey] = it }

            mutex?.lock()

            while (true) {
                logger.trace("Requesting object from endpoint $route")

                globalRatelimitJob?.join()
                response = handler.request(requestBuilder)

                deferred?.apply { complete(response.headers["X-RateLimit-Bucket"] ?: "DEFAULT") }
                    ?.let {
                        ratelimitsMap.getOrPut(formatRatelimitID(route.majorParameter, it.getCompleted())) {
                            Mutex(true)
                        }
                    }
                    ?.also { mutex = it }

                when (response.status) {
                    Unauthorized -> throw IllegalStateException("Invalid token")
                    Forbidden -> {
                        logger.error("Insufficient permissions while requesting ${route.uri}")
                        break
                    }
                    TooManyRequests -> {
                        logger.error("Encountered 429 with route ${route.uri}")

                        if (response.headers["X-RateLimit-Global"] == "true") {
                            globalRatelimitJob = globalRatelimitJob ?: launch {
                                delay(response.headers["Retry-After"]!!.toInt().seconds)
                                globalRatelimitJob = null
                            }

                            globalRatelimitJob!!.join()
                        } else {
                            delay(response.headers["X-RateLimit-Reset-After"]!!.toDouble().seconds)
                        }
                    }
                    else -> break
                }
            }

            if (response.headers["X-RateLimit-Remaining"] != "0") { // pass
                mutex!!.unlock()
            } else launch { // you shall not pass!
                delay(response.headers["X-RateLimit-Reset-After"]!!.toDouble().seconds)
                mutex!!.unlock()
            }
        }

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

    private fun formatRatelimitID(majorParameter: Long?, bucketID: String) = "$bucketID @ $majorParameter"

    override fun close() {
        context.cancel()
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
