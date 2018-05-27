package com.serebit.diskord

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import khttp.get
import okhttp3.OkHttpClient
import okhttp3.Request

const val version = "0.0.0"

fun main(args: Array<String>) {
    val token = args[0]

    val response = get(
        "https://discordapp.com/api/gateway/bot", headers = mapOf(
            "User-Agent" to "DiscordBot (https://github.com/serebit/diskord, $version)",
            "Authorization" to "Bot $token"
        )
    ).let { Gson().fromJson<GatewayResponse>(it.text) }

    println(response)

    OkHttpClient().newWebSocket(
        Request.Builder().url(response.url).build(),
        GatewayAdapter(token)
    )
}

data class GatewayResponse(val url: String, val shards: Int)
