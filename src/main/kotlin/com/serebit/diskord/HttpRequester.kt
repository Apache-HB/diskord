package com.serebit.diskord

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal class HttpRequester(token: String) {
    private val headers = mapOf(
        "User-Agent" to "DiscordBot (https://github.com/serebit/diskord, $version)",
        "Authorization" to "Bot $token",
        "Content-Type" to "application/json"
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

    companion object {
        private const val baseUri = "https://discordapp.com/api/v6"
        val serializer: Gson = GsonBuilder().serializeNulls().create()
    }
}
