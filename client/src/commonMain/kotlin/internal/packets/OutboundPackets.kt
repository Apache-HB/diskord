package com.serebit.strife.internal.packets

import com.serebit.strife.entities.EmbedBuilder
import com.serebit.strife.entities.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==> Channels

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
internal data class CreateChannelInvitePacket(
    val max_age: Int? = null,
    val max_uses: Int? = null,
    val temporary: Boolean? = null,
    val unique: Boolean? = null
)

// ==> User

@Serializable
internal data class ModifyCurrentUserPacket(val username: String? = null, val avatar: String? = null)

@Serializable
internal data class CreateDMPacket(val recipient_id: Long)

// ==> Guild

/**
 * Note: When using the roles parameter, the first member of the array is used to change properties of the
 * guild's @everyone role.
 */
@Serializable
internal data class CreateGuildPacket(
    val name: String,
    val region: String,
    val icon: String? = null,
    val verification_level: Int = 0,
    val default_message_notifications: Int = 0,
    val explicit_content_filter: Int = 0,
    val roles: List<PartialRolePacket> = emptyList(),
    val channels: List<PartialChannelPacket> = emptyList()
) {
    @Serializable
    data class PartialChannelPacket(val name: String, val type: Int)

    @Serializable
    data class PartialRolePacket(
        val id: Int = 0,
        var name: String = "new role",
        var color: Int = 0,
        val hoist: Boolean = false,
        val permissions: Int = 0,
        val mentionable: Boolean = false
    )
}

@Serializable
internal data class ModifyGuildPacket(
    val name: String? = null,
    val region: String? = null,
    val verification_level: Int? = null,
    val default_message_notifications: Int? = null,
    val explicit_content_filter: Int? = null,
    val afk_channel_id: Long? = null,
    val afk_timeout: Int? = null,
    val icon: String? = null,
    val owner_id: Long? = null,
    val splash: String? = null,
    val system_channel_id: Long? = null
)

/** [See](https://discordapp.com/developers/docs/resources/guild#add-guild-member) */
@Serializable
internal data class AddGuildMemberPacket(
    val access_token: String,
    val nick: String? = null,
    val roles: List<Long>? = null,
    val mute: Boolean = false,
    val deaf: Boolean = false
)

@Serializable
internal data class ModifyGuildMemberPacket(
    val nick: String? = null,
    val roles: List<Long>? = null,
    val mute: Boolean? = null,
    val deaf: Boolean? = null,
    val channel_id: Long? = null
)

@Serializable
internal data class CreateGuildChannelPacket(
    val name: String,
    val type: Int? = null,
    val topic: String? = null,
    val bitrate: Int? = null,
    val user_limit: Int? = null,
    val rate_limit_per_user: Int? = null,
    val position: Int? = null,
    val permission_overwrites: List<PermissionOverwritePacket>? = null,
    val parent_id: Long? = null,
    val nsfw: Boolean? = null
)

/** Notes: [hoist] determines whether the role should be displayed separately in the sidebar. */
@Serializable
internal data class CreateGuildRolePacket(
    val name: String? = null,
    val permissions: Int? = null,
    val color: Int = 0,
    val hoist: Boolean = false,
    val mentionable: Boolean = false
)

@Serializable
internal data class ModifyPositionPacket(val id: Long, val position: Int)

@Serializable
internal data class CreateGuildEmojiPacket(
    val name: String,
    val image: String,
    val roles: List<Long>
)

@Serializable
internal data class ModifyGuildEmojiPacket(
    val name: String,
    val roles: List<Long>
)

@Serializable
internal data class ModifyCurrentUserNickPacket(val nick: String)

@Serializable
internal data class CreateGuildIntegrationPacket(val type: String, val id: Long)

@Serializable
internal data class ModifyGuildIntegrationPacket(
    val expire_behavior: Int,
    val expire_grace_period: Int,
    val enable_emoticons: Boolean
)

// ==> Messages

/** An OutBound [MessageSendPacket] takes *at least one* of the two parts of a [Message]: [content] & [embed]. */
@Serializable
internal data class MessageSendPacket(
    val content: String? = null,
    val tts: Boolean? = null,
    val embed: OutgoingEmbedPacket? = null
)

@Serializable
internal data class MessageEditPacket(val content: String? = null, val embed: OutgoingEmbedPacket? = null)

@Serializable
internal data class BulkDeleteMessagesPacket(val messages: List<Long>)

@Serializable
internal data class GetReactionsPacket(
    val before: Long? = null,
    val after: Long? = null,
    val limit: Int? = null
)

@Serializable
internal data class CreateWebhookPacket(
    val name: String,
    val avatar: String? = null
)

@Serializable
internal data class ModifyWebhookPacket(
    val name: String? = null,
    val avatar: String? = null,
    val channel_id: Long? = null
)

@Serializable
internal data class ExecuteWebhookPacket(
    val content: String? = null,
    val username: String? = null,
    val avatar_url: String? = null,
    val tts: Boolean? = null,
    val file: String? = null,
    val embeds: List<OutgoingEmbedPacket>? = null,
    val payload_json: String? = null
)

/** [see](https://discordapp.com/developers/docs/resources/channel#embed-object) */
@Serializable
internal data class OutgoingEmbedPacket(
    val title: String? = "",
    val type: String? = null,
    val description: String? = "",
    @SerialName("url") val titleUrl: String? = null,
    @SerialName("timestamp") private val time_stamp: String? = null,
    @SerialName("color") private val color_int: Int? = null,
    val footer: Footer? = null,
    val image: EmbedGraphic? = null,
    val thumbnail: EmbedGraphic? = null,
    val provider: Provider? = null, // No idea what this means
    val author: Author? = null,
    val fields: List<Field> = emptyList()
) {
    @Serializable
    data class Author(
        val name: String? = null,
        val url: String? = null,
        @SerialName("icon_url") val imgUrl: String? = null,
        @SerialName("proxy_icon_url") val proxyImgUrl: String? = null
    )

    @Serializable
    data class Provider(val name: String? = null, val url: String? = null)

    @Serializable
    data class Field(val name: String, val value: String, val inline: Boolean)

    @Serializable
    data class EmbedGraphic(
        val url: String? = null,
        val proxy_url: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class Footer(
        val text: String?,
        val icon_url: String? = null,
        val proxy_icon_url: String? = null
    )

    init {
        require(fields.size <= EmbedBuilder.FIELD_MAX) {
            "Cannot have ${fields.size} fields. Max fields=${EmbedBuilder.FIELD_MAX}"
        }
    }
}

