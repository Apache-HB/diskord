package com.serebit.diskord

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

const val version = "0.0.0"

fun main(args: Array<String>) {
    val token = args[0]

    val requester = HttpRequester(token)

    val response = requester.get("/gateway/bot").let {
        Gson().fromJson<GatewayResponse>(it.text)
    }

    println(response)

    OkHttpClient().newWebSocket(
        Request.Builder().url(response.url).build(),
        GatewayAdapter(token)
    )
}

data class GatewayResponse(val url: String, val shards: Int)
