package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

/**
 * Information about the current bot application.
 * See https://discordapp.com/developers/docs/topics/oauth2#get-current-application-information
 */
@Serializable
internal data class ApplicationInfoPacket(
    override val id: Long,
    val name: String,
    val description: String,
    val bot_public: Boolean,
    val bot_require_code_grant: Boolean,
    val owner: UserPacket
) : EntityPacket
