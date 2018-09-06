package com.serebit.diskord.internal.network

import com.serebit.diskord.Diskord
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.network.endpoints.Endpoint
import com.serebit.diskord.internal.payloads.IdentifyPayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.DefaultDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.http4k.client.OkHttp
import org.http4k.core.*
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object Requester {
    private val handler = OkHttp()
    private var resetInstant: Instant? = null
    lateinit var token: String
        private set
    private val headers by lazy {
        listOf(
            "User-Agent" to "DiscordBot (${Diskord.sourceUri}, ${Diskord.version})",
            "Authorization" to "Bot $token",
            "Content-Type" to "application/json"
        )
    }
    val identification by lazy {
        IdentifyPayload.Data(
            token, mapOf(
                "\$os" to System.getProperty("os.name"),
                "\$browser" to "diskord",
                "\$device" to "diskord"
            )
        )
    }

    fun initialize(token: String) {
        this.token = token
    }

    inline fun <reified T : Any> requestObject(
        endpoint: Endpoint<T>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): T? = retrieve {
        Logger.trace("Requesting object from endpoint $endpoint")
        request(endpoint, params, data).let { response ->
            checkRateLimit(response)
            if (response.status.successful) {
                JSON.parse<T>(response.bodyString()).also {
                    if (it is Entity) it.cache()
                }
            } else null
        }
    }

    fun requestResponse(
        endpoint: Endpoint<out Any>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): Response = retrieve {
        request(endpoint, params, data).also { checkRateLimit(it) }
    }

    private fun request(
        endpoint: Endpoint<out Any>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): Response {
        val uri = Uri.of("${endpoint.uri}?${params.toList().toUrlFormEncoded()}")
        val body = data?.let { MemoryBody(JSON.stringify(it)) } ?: Body.EMPTY
        return handler(MemoryRequest(endpoint.method, uri, headers, body))
    }

    private fun checkRateLimit(response: Response) {
        resetInstant = response.headers
            .find { it.first == "X-RateLimit-Remaining" && it.second == "0" }
            ?.second
            ?.let { Instant.ofEpochSecond(it.toLong()) }
    }

    private fun <T> retrieve(task: suspend () -> T) = runBlocking {
        withContext(DefaultDispatcher) {
            if (resetInstant != null) {
                delay(ChronoUnit.MILLIS.between(Instant.now(), resetInstant))
            }
            task()
        }
    }
}
