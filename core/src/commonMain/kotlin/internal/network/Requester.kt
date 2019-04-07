package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.io.core.Closeable
import kotlinx.serialization.json.Json

/**
 * An internal object for making REST requests to the Discord API.
 *
 * @property sessionInfo The [SessionInfo] instance used to authorise REST requests.
 * This is also where the [logger] reference is taken from.
 */
internal class Requester(private val sessionInfo: SessionInfo) : Closeable {
    /** The [Requester]'s [HttpClient]. */
    private val handler = HttpClient()
    /** The [Logger] of the [sessionInfo]. */
    private val logger = sessionInfo.logger

    suspend fun <R : Any> sendRequest(route: Route<R>): Response<R> {
        logger.trace("Requesting object from endpoint $route")

        val response = requestHttpResponse(route, route.requestPayload)

        val responseText = try {
            response.readText()
        } catch (ex: ClientRequestException) {
            logger.error("Error in requester: $ex")
            null
        }

        val responseData = responseText?.let {
            Json.nonstrict.parseJson(responseText).jsonObject["code"]?.let {
                logger.error("Request from route $route failed with JSON error code $it")
                null
            } ?: route.serializer?.let {
                Json.nonstrict.parse(it, responseText)
            }
        }

        return Response(response.status, response.version, responseText, responseData)
    }

    /** A private function to make an HTTP Call. */
    private suspend fun <R : Any> requestHttpResponse(
        endpoint: Route<R>,
        payload: RequestPayload
    ): HttpResponse = handler.call(endpoint.uri) {
        method = endpoint.method
        headers.appendAll(sessionInfo.defaultHeaders)
        payload.parameters.map { parameter(it.key, it.value) }
        payload.body?.let { body = it }
    }.response

    override fun close() = handler.close()
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
