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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.map

internal class Requester(private val sessionInfo: SessionInfo) : Closeable {
    private val handler = HttpClient()
    private val json = Json(encodeDefaults = false)
    private val logger = sessionInfo.logger
    
    suspend fun <D : Any, R : Any> sendRequest(endpoint: Route<R>, data: D, dataSerializer: KSerializer<D>) =
        sendRequest(endpoint, generateJsonBody(dataSerializer, data))

    suspend fun <R : Any> sendRequest(route: Route<R>, data: Map<String, String>) =
        sendRequest(route, generateJsonBody(data))

    suspend fun <R : Any> sendRequest(route: Route<R>, data: String) =
        sendRequest(route, generateStringBody(data))

    suspend fun <R : Any> sendRequest(route: Route<R>) = sendRequest<R>(route, null)

    private suspend fun <R : Any> sendRequest(
        route: Route<R>,
        data: TextContent?
    ) : Response<R> {
        logger.trace("Requesting object from endpoint $route")

        val response = requestHttpResponse(route, route.uriParameters, data)

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
        params: Map<String, String>,
        data: TextContent?
    ): HttpResponse = handler.call(endpoint.uri) {
        method = endpoint.method
        headers.appendAll(sessionInfo.defaultHeaders)
        params.map { parameter(it.key, it.value) }
        data?.let { body = it }
    }.response

    private fun <T : Any> generateJsonBody(serializer: KSerializer<T>, data: T) = TextContent(
        json.stringify(serializer, data),
        ContentType.parse("application/json")
    )

    private fun generateJsonBody(data: Map<String, String>) = TextContent(
        json.stringify((StringSerializer to StringSerializer).map, data),
        ContentType.parse("application/json")
    )

    private fun generateStringBody(text: String) = TextContent(text, ContentType.Any)

    override fun close() = handler.close()
}

internal data class Response<T>(
    val status: HttpStatusCode,
    val version: HttpProtocolVersion,
    val text: String,
    val value: T?
)
