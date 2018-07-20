package com.serebit.diskord.network

import com.serebit.diskord.Diskord
import com.serebit.diskord.Serializer
import com.serebit.diskord.network.endpoints.ApiEndpoint
import com.serebit.loggerkt.Logger
import khttp.responses.Response
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.net.HttpURLConnection
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object ApiRequester {
    private var resetInstant: Instant? = null
    lateinit var token: String

    private val headers
        get() = mapOf(
            "User-Agent" to "DiscordBot (https://gitlab.com/serebit/diskord, ${Diskord.version})",
            "Authorization" to "Bot $token",
            "Content-Type" to "application/json"
        )
    val identification
        get() = Payload.Identify.Data(
            token, mapOf(
                "\$os" to System.getProperty("os.name"),
                "\$browser" to "diskord",
                "\$device" to "diskord"
            )
        )

    inline fun <reified T : Any> requestObject(
        endpoint: ApiEndpoint<T>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): Deferred<T?> = retrieve {
        Logger.trace("Requesting object from endpoint $endpoint")
        when (endpoint) {
            is ApiEndpoint.Get -> khttp.get(endpoint.uri, headers, params)
            is ApiEndpoint.Post -> khttp.post(endpoint.uri, headers, params, data)
            is ApiEndpoint.Put -> khttp.put(endpoint.uri, headers, params, data)
        }.let {
            checkRateLimit(it)
            if (it.statusCode == HttpURLConnection.HTTP_OK) Serializer.fromJson<T>(it.text) else null
        }
    }

    fun request(
        endpoint: ApiEndpoint<out Any>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): Deferred<Response> =
        retrieve {
            when (endpoint) {
                is ApiEndpoint.Get -> khttp.get(endpoint.uri, headers, params)
                is ApiEndpoint.Post -> khttp.post(endpoint.uri, headers, params, data)
                is ApiEndpoint.Put -> khttp.put(endpoint.uri, headers, params, data)
            }.also { checkRateLimit(it) }
        }

    private fun checkRateLimit(response: Response) {
        resetInstant = if (response.headers["X-RateLimit-Remaining"] == "0") {
            response.headers["X-RateLimit-Reset"]?.let {
                Instant.ofEpochSecond(it.toLong())
            }
        } else null
    }

    private fun <T> retrieve(task: suspend () -> T) = async {
        if (resetInstant != null) {
            delay(ChronoUnit.MILLIS.between(Instant.now(), resetInstant))
        }
        task()
    }
}
