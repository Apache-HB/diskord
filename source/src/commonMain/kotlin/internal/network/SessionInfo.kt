package com.serebit.diskord.internal.network

import com.serebit.diskord.Context
import com.serebit.diskord.internal.IdentifyPayload
import com.serebit.diskord.internal.osName
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
        "User-Agent" to listOf("DiscordBot (${Context.sourceUri}, ${Context.version})"),
        "Authorization" to listOf("Bot $token")
    )
}
