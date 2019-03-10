package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.io.core.Closeable
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.map

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

    /** Make a REST request to the API using an [Endpoint] with optional additional [params] and/or [data]. */
    suspend fun <T : Any> sendRequest(
        endpoint: Endpoint<T>,
        params: Map<String, String> = emptyMap(),
        data: Map<String, String> = emptyMap()
    ): Response<T> {
        logger.trace("Requesting object from endpoint $endpoint")

        val response = requestHttpResponse(endpoint, params, data)

        val responseText = response.readText()
        val responseData = endpoint.serializer?.let { serializer ->
            try {
                Json.nonstrict.parse(serializer, responseText)
            } catch (ex: Exception) {
                ex.message?.let { logger.error(it) }
                null
            }
        }

        return Response(response.status, response.version, responseText, responseData)
    }

    /** A private function to make an [HTTP Call][io.ktor.client.call.HttpClientCall]. */
    private suspend fun <T : Any> requestHttpResponse(
        endpoint: Endpoint<T>,
        params: Map<String, String>,
        data: Map<String, String>
    ): HttpResponse = handler.call(endpoint.uri) {
        method = endpoint.method
        headers.appendAll(sessionInfo.defaultHeaders)
        params.map { parameter(it.key, it.value) }
        if (data.isNotEmpty()) body = generateBody(data)
    }.response

    private fun generateBody(data: Map<String, String>) = TextContent(
        Json.stringify((StringSerializer to StringSerializer).map, data),
        ContentType.parse("application/json")
    )

    /** Close the [Requester] and it's underlying [HttpClient]. */
    override fun close() = handler.close()
}

/** An object to hold the [typed][T] response to a REST request made by a [Requester]. */
internal data class Response<T>(
    val status: HttpStatusCode,
    val version: HttpProtocolVersion,
    val text: String,
    val value: T?
)
