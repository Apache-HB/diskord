package com.serebit.diskord.internal.network

import com.serebit.diskord.Diskord
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.network.endpoints.Endpoint
import com.serebit.diskord.internal.payloads.IdentifyPayload
import com.serebit.logkat.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.parameter
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import kotlinx.coroutines.io.readRemaining
import kotlinx.coroutines.runBlocking

internal actual object Requester {
    private val handler = HttpClient()
    lateinit var token: String
        private set
    private val headers by lazy {
        headersOf(
            "User-Agent" to listOf("DiscordBot (${Diskord.sourceUri}, ${Diskord.version})"),
            "Authorization" to listOf("Bot $token")
        )
    }
    actual val identification by lazy {
        IdentifyPayload.Data(
            token, mapOf(
                "\$os" to System.getProperty("os.name"),
                "\$browser" to "diskord",
                "\$device" to "diskord"
            )
        )
    }

    actual fun initialize(token: String) {
        this.token = token
    }

    actual inline fun <reified T : Any> requestObject(
        endpoint: Endpoint<T>,
        params: Map<String, String>,
        data: Any?
    ): T? = runBlocking {
        Logger.trace("Requesting object from endpoint $endpoint")
        request(endpoint, params, data).let { response ->
            if (response.status.isSuccess()) {
                JSON.parse<T>(response.content.readRemaining().readText())
            } else null
        }
    }

    actual fun requestResponse(
        endpoint: Endpoint<out Any>,
        params: Map<String, String>,
        data: Any?
    ): HttpResponse = request(endpoint, params, data)

    private fun request(
        endpoint: Endpoint<out Any>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): HttpResponse = runBlocking {
        handler.call(endpoint.uri) {
            method = endpoint.method
            headers.appendAll(this@Requester.headers)
            params.map { parameter(it.key, it.value) }
            data?.let { body = TextContent(JSON.stringify(it), contentType = ContentType.parse("application/json")) }
        }.response
    }
}
