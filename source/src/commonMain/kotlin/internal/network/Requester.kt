package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.network.endpoints.Endpoint
import com.serebit.diskord.internal.runBlocking
import com.serebit.logkat.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.io.readRemaining
import kotlinx.io.core.Closeable
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.map

internal class Requester(private val sessionInfo: SessionInfo, val logger: Logger) : Closeable {
    private val handler = HttpClient()

    inline fun <reified T : Any> requestObject(
        endpoint: Endpoint.ObjectData<T>,
        params: Map<String, String> = emptyMap(),
        data: Map<String, String> = emptyMap()
    ): T? = runBlocking {
        logger.trace("Requesting object from endpoint $endpoint")

        val response = requestResponse(endpoint, params, data)

        if (response.status.isSuccess()) {
            JSON.parse(endpoint.serializer, response.content.readRemaining().readText())
        } else null
    }

    fun sendRequest(
        endpoint: Endpoint,
        params: Map<String, String> = emptyMap(),
        data: Map<String, String> = emptyMap()
    ): Boolean = requestResponse(endpoint, params, data).status.isSuccess()

    fun requestResponse(
        endpoint: Endpoint,
        params: Map<String, String> = emptyMap(),
        data: Map<String, String> = emptyMap()
    ): HttpResponse = runBlocking {
        logger.trace("Sending request to endpoint $endpoint")
        handler.call(endpoint.uri) {
            method = endpoint.method
            headers.appendAll(sessionInfo.defaultHeaders)
            params.map { parameter(it.key, it.value) }
            if (data.isNotEmpty()) body = generateBody(data)
        }.response
    }

    private fun generateBody(data: Map<String, String>) = TextContent(
        JSON.stringify((StringSerializer to StringSerializer).map, data),
        ContentType.parse("application/json")
    )

    override fun close() = handler.close()
}
