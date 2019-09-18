package com.serebit.strife.internal.packets

import com.serebit.strife.entities.EmbedBuilder
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.packets.CreateGuildPacket.PartialChannelPacket
import com.serebit.strife.internal.packets.OutgoingEmbedPacket.*
import com.soywiz.klock.DateTimeTz
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
internal data class GetChannelMessagesPacket(
    val around: Long? = null,
    val before: Long? = null,
    val after: Long? = null,
    val limit: Int? = null
)

@Serializable
internal data class CreateChannelInvitePacket(
    val max_age: Int = 86400,
    val max_uses: Int = 0,
    val temporary: Boolean = false,
    val unique: Boolean = false
)

// ==> User

@Serializable
internal data class ModifyCurrentUserPacket(
    val username: String? = null,
    val avatar: String? = null
)

@Serializable
internal data class CreateDMPacket(val recipient_id: Long)

// ==> Guild

/**
 * TODO
 *
 * @property name
 * @property region
 * @property icon
 * @property verification_level
 * @property default_message_notifications
 * @property explicit_content_filter
 * @property roles When using the roles parameter, the first member of the array is used to change properties of the
 * guild's @everyone role.
 * @property channels list of [PartialChannelPacket]
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

/**
 * [See](https://discordapp.com/developers/docs/resources/guild#add-guild-member)
 *
 * @property access_token an oauth2 access token granted with the guilds.join to the bot's application for the user you
 * want to add to the guild
 * @property nick
 * @property roles
 * @property mute
 * @property deaf
 */
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

@Serializable
internal data class ModifyGuildChannelPositionsPacket(
    val id: Long,
    val position: Int
)

/**
 * TODO
 *
 * @property name
 * @property permissions
 * @property color
 * @property hoist whether the role should be displayed separately in the sidebar.
 * @property mentionable
 */
@Serializable
internal data class CreateGuildRolePacket(
    val name: String? = null,
    val permissions: Int? = null,
    val color: Int = 0,
    val hoist: Boolean = false,
    val mentionable: Boolean = false
)

@Serializable
internal data class ModifyGuildRolePositionPacket(val roleID: Long, val position: Int)

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

/**
 * An OutBound [MessageSendPacket] takes *at least one* of the two parts of a [Message]: [content] & [embed].
 * @property content The text content of the [Message] (non-embed)
 * @property embed The [OutgoingEmbedPacket] of the [Message]
 * @property tts Text-To-Speech
 */
@Serializable
internal data class MessageSendPacket(
    val content: String? = null,
    val tts: Boolean? = null,
    val embed: OutgoingEmbedPacket? = null
) {
    init {
        require(content != null || embed != null) { "Content & OutgoingEmbedPacket cannot both be null." }
    }
}

/** "All parameters to this endpoint are optional." */
@Serializable
internal data class MessageEditPacket(val content: String? = null, val embed: OutgoingEmbedPacket? = null)

@Serializable
internal data class BulkDeleteMessagesPacket(val messages: List<Long>)

@Serializable
internal data class GetReactionsPacket(
    val before: Long? = null,
    val after: Long? = null,
    val limit: Int = 25
)

/**
 * An [OutgoingEmbedPacket] is a card-like content display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n)
 * you can see each part of the embed explained and shown.
 *
 * You can use an embed preview tool [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 *
 * @property title The title of the embed appears atop the [description] and right below the [author].
 * @property titleUrl The url which when the [title] is clicked will be opened. Set this to `null` for no link.
 * @property description The description of the embed appears after the [title] and before any [Field]. The
 * [description] supports standard Discord markdown as well as [markdown\](links).
 * @property thumbnail The thumbnail appears in the upper-right-hand corner of the embed as a smaller image. Set this
 * to `null` for no thumbnail.
 * @property author The author who's name will appear at the very top of the [OutgoingEmbedPacket]. The [Author.imgUrl]
 * will be shown to the left of the [Author.name] (in the very top-left corner of the [OutgoingEmbedPacket]).
 * @property provider TODO Discord refuses to explain what this is
 * @property fields A [List] of all [Field]s in the [OutgoingEmbedPacket] in order of appearance
 * (top -> bottom, left -> right).
 * @property image The [EmbedGraphic] which is shown at the bottom of the embed as a large image.
 * @property color_int The color of the [OutgoingEmbedPacket]'s left border. Leaving this `null` will result in the
 * default greyish color.
 * @property footer The [Footer] of the embed shown at the very bottom.
 * @property time_stamp The timestamp is shown to the right of the [footer] and is usually used to mark when the embed
 * was sent, but can be set to any [DateTimeTz].
 */
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

    /**
     * A [Field] is a titled paragraph displayed in order under the [description].
     *
     * @property name The title of the [Field].
     * @property value The text displayed under the [name]
     * @property inline Whether the [Field] should be displayed inline (i.e., next to another inline [Field] where
     * possible).
     */
    @Serializable
    data class Field(val name: String, val value: String, val inline: Boolean)

    /** An image or video within the [OutgoingEmbedPacket]. */
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

