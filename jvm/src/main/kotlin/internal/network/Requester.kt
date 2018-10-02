package com.serebit.diskord.internal.network

import com.serebit.diskord.Diskord
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.cache
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
import io.ktor.util.flattenEntries
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.io.readRemaining
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit

internal actual object Requester {
    private val handler = HttpClient()
    private var resetInstant: Instant? = null
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
    ): T? = retrieve {
        Logger.trace("Requesting object from endpoint $endpoint")
        request(endpoint, params, data).let { response ->
            checkRateLimit(response)
            if (response.status.isSuccess()) {
                JSON.parse<T>(response.content.readRemaining().readText()).also {
                    if (it is Entity) it.cache()
                }
            } else null
        }
    }

    actual fun requestResponse(
        endpoint: Endpoint<out Any>,
        params: Map<String, String>,
        data: Any?
    ): HttpResponse = retrieve {
        request(endpoint, params, data).also { checkRateLimit(it) }
    }

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

    private fun checkRateLimit(response: HttpResponse) {
        resetInstant = response.headers.flattenEntries()
            .find { it.first == "X-RateLimit-Remaining" && it.second == "0" }
            ?.second
            ?.let { Instant.ofEpochSecond(it.toLong()) }
    }

    private fun <T> retrieve(task: suspend () -> T) = runBlocking {
        withContext(Dispatchers.Default) {
            if (resetInstant != null) {
                delay(ChronoUnit.MILLIS.between(Instant.now(), resetInstant))
            }
            task()
        }
    }
}
