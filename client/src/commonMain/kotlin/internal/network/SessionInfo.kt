package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.StrifeInfo
import com.serebit.strife.internal.IdentifyPayload
import com.serebit.strife.internal.osName
import io.ktor.http.headersOf

/**
 * A data object used to hold information about the current client session.
 *
 * @property token The client token. This can be found in the application's "bot" section:
 * https://discordapp.com/developers/applications/{BOT_ID}/bots
 * @property logger The [Logger] to be used for this session. This [Logger] is passed around to other objects.
 */
internal data class SessionInfo(val token: String, val logger: Logger) {
    val identification = IdentifyPayload.Data(
        token, mapOf(
            "\$os" to osName,
            "\$browser" to "strife",
            "\$device" to "strife"
        )
    )
    val defaultHeaders = headersOf(
        "User-Agent" to listOf("DiscordBot (${StrifeInfo.sourceUri}, ${StrifeInfo.version})"),
        "Authorization" to listOf("Bot $token")
    )
}
