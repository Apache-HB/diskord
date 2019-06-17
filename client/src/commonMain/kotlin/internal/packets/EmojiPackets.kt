package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class GenericEmojiPacket(
    val id: Long?,
    val name: String,
    val roles: List<Long> = emptyList(),
    val user: UserPacket? = null,
    val require_colons: Boolean = false,
    val managed: Boolean = false,
    val animated: Boolean = false
)

@Serializable
internal data class GuildEmojiPacket(
    override val id: Long,
    val name: String,
    val roles: List<Long> = emptyList(),
    val user: UserPacket? = null,
    val require_colons: Boolean = false,
    val managed: Boolean = false,
    val animated: Boolean = false
) : EntityPacket

/**
 * [see](https://discordapp.com/developers/docs/resources/emoji#emoji-object-gateway-reaction-standard-emoji-example)
 */
@Serializable
internal data class PartialEmojiPacket(val id: Long?, val name: String)

