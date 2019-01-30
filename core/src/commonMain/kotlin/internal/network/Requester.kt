package com.serebit.strife.internal.network

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

internal class Requester(private val sessionInfo: SessionInfo) : Closeable {
    private val handler = HttpClient()
    private val logger = sessionInfo.logger

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

    override fun close() = handler.close()
}

internal data class Response<T>(
    val status: HttpStatusCode,
    val version: HttpProtocolVersion,
    val text: String,
    val value: T?
)
