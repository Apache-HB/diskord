package com.serebit.diskord.network

import com.serebit.diskord.Diskord
import com.serebit.diskord.EntityCache
import com.serebit.diskord.JSON
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.network.endpoints.Endpoint
import com.serebit.diskord.network.payloads.IdentifyPayload
import com.serebit.loggerkt.Logger
import khttp.responses.Response
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import java.net.HttpURLConnection
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object Requester {
    private var resetInstant: Instant? = null
    lateinit var token: String
        private set

    private val headers by lazy {
        mapOf(
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
            if (response.statusCode == HttpURLConnection.HTTP_OK) {
                JSON.parse<T>(response.text).also {
                    if (it is Entity) EntityCache.cache(it)
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
    ): Response = when (endpoint) {
        is Endpoint.Get -> khttp.get(endpoint.uri, headers, params)
        is Endpoint.Post -> khttp.post(endpoint.uri, headers, params, data)
        is Endpoint.Put -> khttp.put(endpoint.uri, headers, params, data)
        is Endpoint.Patch -> khttp.patch(endpoint.uri, headers, params, data)
        is Endpoint.Delete -> khttp.delete(endpoint.uri, headers, params, data)
    }

    private fun checkRateLimit(response: Response) {
        resetInstant = if (response.headers["X-RateLimit-Remaining"] == "0") {
            response.headers["X-RateLimit-Reset"]?.let {
                Instant.ofEpochSecond(it.toLong())
            }
        } else null
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
