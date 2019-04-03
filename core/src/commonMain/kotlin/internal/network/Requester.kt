package com.serebit.strife.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.io.core.Closeable
import kotlinx.serialization.json.Json

internal class Requester(private val sessionInfo: SessionInfo) : Closeable {
    private val handler = HttpClient()
    private val logger = sessionInfo.logger

    suspend fun <R : Any> sendRequest(route: Route<R>): Response<R> {
        logger.trace("Requesting object from endpoint $route")

        val response = requestHttpResponse(route, route.requestPayload)

        val responseText = response.readText()
        val responseData = route.serializer?.let { serializer ->
            try {
                Json.nonstrict.parse(serializer, responseText)
            } catch (ex: Exception) {
                ex.message?.let { logger.error("$it in Requester") }
                null
            }
        }

        return Response(response.status, response.version, responseText, responseData)
    }

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

internal data class Response<T>(
    val status: HttpStatusCode,
    val version: HttpProtocolVersion,
    val text: String,
    val value: T?
)
