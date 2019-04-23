package com.serebit.strife.entities

import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.Embed.*
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.EmbedPacket
import com.serebit.strife.internal.packets.MessageEditPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Represents a textual message sent in a [TextChannel]. A [Message] can consist of text, files, and embeds.
 *
 * @constructor Encapsulates a [MessageData] instance in an end-user-facing [Message] instance
 */
class Message internal constructor(private val data: MessageData) : Entity {
    override val id = data.id
    override val context = data.context

    /**
     * The [User] who sent this [Message]. If the message was sent by the system,
     * no [User] is associated with it and this property will be `null`.
     */
    val author: User? get() = data.author.toEntity()
    /** The [TextChannel] this [Message] was sent to. */
    val channel: TextChannel get() = data.channel.toEntity()
    /** The [message's][Message] text content excluding attachments and embeds */
    val content: String get() = data.content
    /**
     * The [date and time][DateTimeTz] at which this [Message] was last edited.
     * If the [Message] has never been edited, this will be `null`.
     */
    val editedAt: DateTimeTz? get() = data.editedAt
    /** A [List] of mentioned [users][User] ordered by appearance, i.e.: @User_1, @User_2, @User_3`,... */
    val userMentions: List<User> get() = data.mentionedUsers.map { it.toEntity() }
    /** A [List] of mentioned [roles][Role] ordered by appearance, i.e.: @Role_1, @Role_2, @Role_3`,... */
    val roleMentions: List<Role> get() = data.mentionedRoles.map { it.toEntity() }
    /** `true` if the message contains an `@everyone` ping (which requires [Permission.MentionEveryone]). */
    val mentionsEveryone: Boolean get() = data.mentionsEveryone
    /** `true` if this [Message] is currently pinned in the [channel] */
    val isPinned: Boolean get() = data.isPinned
    /** `true` if the [Message] was sent as a Text-to-Speech message (`/tts`). */
    val isTextToSpeech get() = data.isTextToSpeech
    /** A [List] of all embeds in this [Message]. */
    val embeds get() = data.embeds.map { it.toEmbed() }

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(text: String): Message? {
        require(text.length in 1..MAX_LENGTH)
        return context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(text)))
            .value
            ?.toData(context)
            ?.toEntity()
    }

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(embed = embed.build())))
            .value
            ?.toData(context)
            ?.toEntity()

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(text: String, embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(text, embed.build())))
            .value
            ?.toData(context)
            ?.toEntity()

    /** Delete this [Message]. *Requires client is [author] or [Permission.ManageMessages].* */
    suspend fun delete(): Boolean =
        context.requester.sendRequest(Route.DeleteMessage(channel.id, id)).status.isSuccess()

    override fun equals(other: Any?) = other is Message && other.id == id

    /** [see](https://discordapp.com/developers/docs/resources/channel#message-object-message-types). */
    enum class MessageType(val value: Int) {
        /** The [MessageType] for normal [Messages][Message] sent by bots or [Users][User]. */
        DEFAULT(0),
        RECIPIENT_ADD(1), RECIPIENT_REMOVE(2),
        CALL(3),
        CHANNEL_NAME_CHANGE(4), CHANNEL_ICON_CHANGE(5), CHANNEL_PINNED_MESSAGE(6),
        GUILD_MEMBER_JOIN(7)
    }

    companion object {
        /** The maximum number of characters allowed in a single [Message]. */
        const val MAX_LENGTH = 2000
    }
}

/** Reply to this message with the given [text]. */
suspend fun Message.reply(text: String) = channel.send(text)

/** Reply to this message with the given [embed]. */
suspend fun Message.reply(embed: EmbedBuilder) = channel.send(embed)

/** Reply to this message with the given [text] and [embed]. */
suspend fun Message.reply(text: String, embed: EmbedBuilder): Message? = channel.send(text, embed)

/** Reply to this message with the given [embed]. */
suspend fun Message.reply(embed: EmbedBuilder.() -> Unit) = channel.send(embed)

/** Reply to this message with the given [text] and [embed] */
suspend fun Message.reply(text: String, embed: EmbedBuilder.() -> Unit) = channel.send(text, embed)

/** Edit this message, replacing it with the given [embed]. */
suspend inline fun Message.edit(embed: EmbedBuilder.() -> Unit) = edit(EmbedBuilder().apply(embed))

/** Edit this message, replacing it with the given [text] and [embed]. */
suspend inline fun Message.edit(text: String, embed: EmbedBuilder.() -> Unit) = edit(text, EmbedBuilder().apply(embed))

/** Returns `true` if the given [text] is in this [Message]'s [content][Message.content]. */
operator fun Message.contains(text: String) = text in content

/**
 * An embed is a card-like content display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n) you can see
 * each part of the embed explained and shown.
 *
 * You can use an embed preview tool [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 *
 * @property title The title of the embed appears atop the [description] and right below the [author].
 * @property description The description of the embed appears after the [title] and before any [Field]. The
 * description supports standard Discord markdown as well as [markdown\](links).
 * @property thumbnail The thumbnail appears in the upper-right-hand corner of the embed as a smaller image. If this is
 * `null`, the embed has no thumbnail.
 * @property author The author whose name and image will appear at the top-left corner of the embed.
 * @property fields A list of all [Field]s in the embed in order of appearance (left -> right, top -> bottom).
 * @property image The large image which is shown at the bottom of the embed. If this is `null`, the embed has no image.
 * @property video The large video which is shown at the bottom of the embed. If this is `null`, the embed has no video.
 * @property color The color of the embed's left border.
 * @property footer The footer of the embed shown at the very bottom.
 * @property timestamp The timestamp is shown to the right of the [footer] and is usually used to mark when the embed
 * was sent, but can be set to any date and time.
 */
data class Embed internal constructor(
    val author: Author? = null,
    val title: Title? = null,
    val description: String? = null,
    val fields: List<Field> = emptyList(),
    val color: Color = Color.BLACK,
    val image: Graphic? = null,
    val thumbnail: Graphic? = null,
    val video: Graphic? = null,
    val footer: Footer? = null,
    val timestamp: DateTime? = null
) {

    /**
     * @property text The text of the embed appears atop the [description] and right below the [author].
     * @property url The url which when the [title] is clicked will be opened. Set this to `null` for no link.
     */
    @Serializable
    data class Title internal constructor(val text: String, val url: String? = null) {
        override fun toString() = text
    }

    /**
     * The Author of [Embed] shown at the top. [see](https://i.imgur.com/JgZtxIM.png)
     *
     * @property name The Author's name
     * @property url The url hyperlink of the [name]
     * @property imgUrl The url to the image/avatar
     * @property proxyImgUrl
     */
    @Serializable
    data class Author internal constructor(
        val name: String? = null, val url: String? = null, val imgUrl: String? = null, val proxyImgUrl: String? = null
    )

    /**
     * A [Field] is a titled paragraph displayed, in order, under the [description].
     *
     * @property name The title of the [Field].
     * @property value The text displayed under the [name]
     * @property inline Whether the [Field] should be displayed inline
     * (i.e., next to another inline [Field] where possible).
     */
    @Serializable
    data class Field internal constructor(val name: String, val value: String, val inline: Boolean)

    /** An image or video within the embed. */
    @Serializable
    data class Graphic internal constructor(
        val url: String? = null, val proxyUrl: String? = null, val height: Short? = null, val width: Short? = null
    )

    /**
     * The footer of the [Embed]. [see](https://i.imgur.com/jdf4sbi.png).
     *
     * @property text The text of the footer.
     * @property iconUrl The url of the icon.
     */
    @Serializable
    data class Footer internal constructor(
        val text: String?, val iconUrl: String? = null, val proxyIconUrl: String? = null
    )

}

/** Convert an [EmbedPacket] to a user-facing [Embed]. */
internal fun EmbedPacket.toEmbed() = Embed(
    author?.let { Author(it.name, it.url, it.icon_url, it.proxy_icon_url) },
    title?.let { Title(it, this@toEmbed.url) },
    description,
    fields?.let { list -> list.map { f -> Field(f.name, f.value, f.inline ?: false) } } ?: emptyList(),
    color?.let { Color(it) } ?: Color.BLACK, // TODO Default discord grey? https://discordapp.com/branding
    image?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    thumbnail?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    video?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    footer?.let { Footer(it.text, it.icon_url, it.proxy_icon_url) },
    timestamp?.let { DateFormat.ISO_WITH_MS.tryParse(it)?.local }
)
