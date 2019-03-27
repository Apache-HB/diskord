package com.serebit.strife.internal.packets

import com.serebit.strife.entities.Embed
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.requireAllNotNull
import kotlinx.serialization.Serializable

/**
 * An OutBound [MessageSendPacket] takes *at least one* of the two parts of a [Message]: [content] & [embed].
 * @property content The text content of the [Message] (non-embed)
 * @property embed The [Embed] of the [Message]
 * @property tts Text-To-Speech
 */
@Serializable
internal data class MessageSendPacket(
    val content: String? = null,
    val tts: Boolean? = null,
    val embed: Embed? = null
) {
    init {
        requireAllNotNull(content, embed) { "Content & Embed cannot both be null." }
    }
}

/** "All parameters to this endpoint are optional." */
@Serializable
internal data class MessageEditPacket(val content: String? = null, val embed: Embed? = null)

@Serializable
internal data class ModifyChannelPacket(
    val name: String? = null,
    val position: Int? = null,
    val topic: String? = null,
    val nsfw: Boolean? = null,
    val rate_limit_per_user: Int? = null,
    val bitrate: Int? = null,
    val user_limit: Int? = null,
    val permission_overwrites: List<PermissionOverwritePacket>? = null,
    val parent_id: Long? = null
)

@Serializable
internal data class GetChannelMessagesPacket(
    val around: Long? = null,
    val before: Long? = null,
    val after: Long? = null,
    val limit: Int? = null
)
