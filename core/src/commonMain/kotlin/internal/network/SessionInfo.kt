package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.StrifeInfo
import com.serebit.strife.internal.IdentifyPayload
import com.serebit.strife.internal.osName
import io.ktor.http.headersOf

internal data class SessionInfo(val token: String, val libName: String, val logger: Logger) {
    val identification = IdentifyPayload.Data(
        token, mapOf(
            "\$os" to osName,
            "\$browser" to libName,
            "\$device" to libName
        )
    )
    val defaultHeaders = headersOf(
        "User-Agent" to listOf("DiscordBot (${StrifeInfo.sourceUri}, ${StrifeInfo.version})"),
        "Authorization" to listOf("Bot $token")
    )
}
