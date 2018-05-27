package com.serebit.diskord

import khttp.get

const val version = "0.0.0"

fun main(args: Array<String>) {
    val token = args[0]

    val response = get("https://discordapp.com/api/gateway/bot", headers = mapOf(
        "User-Agent" to "DiscordBot (https://github.com/serebit/diskord, $version)",
        "Authorization" to "Bot $token"
    ))

    println(response.text)
}
