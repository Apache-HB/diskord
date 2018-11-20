package com.serebit.diskord.internal.network

import com.serebit.diskord.Bot
import com.serebit.diskord.internal.osName
import com.serebit.diskord.internal.payloads.IdentifyPayload
import io.ktor.http.headersOf

internal data class SessionInfo(
    val token: String,
    val libName: String
) {
    val identification = IdentifyPayload.Data(
        token, mapOf(
            "\$os" to osName,
            "\$browser" to libName,
            "\$device" to libName
        )
    )
    val defaultHeaders = headersOf(
        "User-Agent" to listOf("DiscordBot (${Bot.sourceUri}, ${Bot.version})"),
        "Authorization" to listOf("Bot $token")
    )
}
