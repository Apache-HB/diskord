package com.serebit.diskord.network

import com.serebit.diskord.Serializer
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.version
import khttp.responses.Response
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Delay
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.scheduling.ExperimentalCoroutineDispatcher
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

internal object ApiRequester {
    private const val apiVersion = 6
    private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    private val rateLimiter = ExperimentalCoroutineDispatcher().blocking(5)
    lateinit var token: String

    private val headers
        get() = mapOf(
            "User-Agent" to "DiscordBot (https://gitlab.com/serebit/diskord, $version)",
            "Authorization" to "Bot $token",
            "Content-Type" to "application/json"
        )
    val identification
        get() = Payload.Identify.Data(
            token, mapOf(
                "\$os" to "linux",
                "\$browser" to "diskord",
                "\$device" to "diskord"
            )
        )

    inline fun <reified T : Any> get(endpoint: String, params: Map<String, String> = mapOf()): Deferred<T?> =
        async(rateLimiter) {
            get(endpoint, params).let {
                checkRateLimit(it)
                if (it.statusCode == 200) Serializer.fromJson<T>(it.text) else null
            }
        }

    fun get(endpoint: String, params: Map<String, String> = mapOf()) =
        khttp.get("$baseUri$endpoint", headers, params)

    fun put(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) =
        khttp.put("$baseUri$endpoint", headers, params, data)

    fun post(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) = async(rateLimiter) {
        khttp.post("$baseUri$endpoint", headers, params, data).also { checkRateLimit(it) }
    }

    fun patch(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) =
        khttp.patch("$baseUri$endpoint", headers, params, data)

    fun delete(endpoint: String) =
        khttp.delete("$baseUri$endpoint", headers)

    private suspend fun checkRateLimit(response: Response) {
        if (response.headers["X-RateLimit-Remaining"] == "0" && rateLimiter is Delay) {
            response.headers["X-RateLimit-Reset"]?.let {
                val limit = ChronoUnit.SECONDS.between(OffsetDateTime.now(), Instant.ofEpochSecond(it.toLong()))
                rateLimiter.delay(limit, TimeUnit.SECONDS)
            }
        }
    }
}
