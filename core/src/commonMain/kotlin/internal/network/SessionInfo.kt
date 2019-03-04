package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.Context
import com.serebit.strife.internal.IDentifyPayload
import com.serebit.strife.internal.osName
import io.ktor.http.headersOf

/**
 * A data object used to hold information about the current client session.
 *
 * @property token The client token. This can be found in the application's "bot" section:
 * https://discordapp.com/developers/applications/{BOT_ID}/bots
 * @property logger The [Logger] to be used for this session. This [Logger] is passed around to other objects.
 */
internal data class SessionInfo(val token: String, val libName: String, val logger: Logger) {
    val identification = IDentifyPayload.Data(
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
