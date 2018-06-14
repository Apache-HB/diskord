package com.serebit.diskord.network

import com.serebit.diskord.Serializer
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.version

internal object ApiRequester {
    private const val apiVersion = 6
    private const val baseUri = "https://discordapp.com/api/v$apiVersion"
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

    inline fun <reified T : Any> get(endpoint: String, params: Map<String, String> = mapOf()): T? =
        get(endpoint, params).let {
            if (it.statusCode == 200) Serializer.fromJson(it.text) else null
        }


    fun get(endpoint: String, params: Map<String, String> = mapOf()) =
        khttp.get("$baseUri$endpoint", headers, params)

    fun put(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) =
        khttp.put("$baseUri$endpoint", headers, params, data)

    fun post(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) =
        khttp.post("$baseUri$endpoint", headers, params, data)

    fun patch(endpoint: String, params: Map<String, String> = mapOf(), data: Any? = null) =
        khttp.patch("$baseUri$endpoint", headers, params, data)

    fun delete(endpoint: String) =
        khttp.delete("$baseUri$endpoint", headers)
}
