package com.serebit.strife.internal.network

import com.serebit.strife.Context
import com.serebit.strife.internal.IdentifyPayload
import com.serebit.strife.internal.osName
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
