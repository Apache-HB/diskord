package com.serebit.strife.entities

import com.serebit.strife.StrifeDsl
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.Embed.*
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route.DeleteMessage
import com.serebit.strife.internal.network.Route.EditMessage
import com.serebit.strife.internal.packets.EmbedPacket
import com.serebit.strife.internal.packets.MessageEditPacket
import com.serebit.strife.internal.packets.OutgoingEmbedPacket
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
    /** A [List] of all [Embeds][OutgoingEmbedPacket] in this [Message]. */
    val embeds get() = data.embeds.map { it.toEmbedBuilder() }

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(text: String?, embed: EmbedBuilder?): Message? {
        require(text != null || embed != null) {
            "Message#edit must include text and/or embed."
        }
        return context.requester.sendRequest(EditMessage(channel.id, id, MessageEditPacket(text, embed = embed?.build())))
            .value?.toData(context)?.toEntity()
    }

    /** Delete this [Message]. *Requires client is [author] or [Permission.ManageMessages].* */
    suspend fun delete(): Boolean =
        context.requester.sendRequest(DeleteMessage(channel.id, id)).status.isSuccess()

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

/** Send a new [Message] to the [channel]. */
suspend fun Message.reply(text: String) = reply(text, null)

/** Send an [OutgoingEmbedPacket] to the [channel]. */
suspend fun Message.reply(embed: EmbedBuilder) = reply("", embed)

/** Send a [Message] to the [channel] using a [MessageBuilder]. */
suspend fun Message.reply(messageBuilder: MessageBuilder.() -> Unit) = channel.send(messageBuilder)

/** Send a [Message] to the [channel]. */
suspend fun Message.reply(text: String, embed: EmbedBuilder? = null): Message? = channel.send(text, embed)

/** Edit this [Message]. This can only be done when the client is the [author]. */
suspend fun Message.edit(text: String) = edit(text, null)

/** Edit this [Message]. This can only be done when the client is the [author]. */
suspend fun Message.edit(embed: EmbedBuilder) = edit(null, embed)

/** Edit this [Message]. This can only be done when the client is the [author]. */
suspend fun Message.edit(messageBuilder: MessageBuilder.() -> Unit) =
    MessageBuilder().apply(messageBuilder).let { edit(it.text, it.embed) }

/** Returns `true` if the given [text] is in this [Message]'s [content]. */
operator fun Message.contains(text: String) = text in content


/** Used to make a message using DSL. */
class MessageBuilder {
    /** The [Message.content]. */
    var text: String? = null
    /** A message embed. One per message! */
    var embed: EmbedBuilder? = null
    var tts: Boolean? = null

    /** Set the embed of the [Message]. */
    @EmbedDsl
    fun embed(builder: EmbedBuilder.() -> Unit) {
        embed = com.serebit.strife.entities.embed(builder)
    }
}

/** Build a [Message] which can be sent using [TextChannel.send] or [Message.reply]. */
@StrifeDsl
fun message(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder)

/**
 * An [Embed] is a card-like content display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n) you can see
 * each part of the embed explained and shown.
 *
 * You can use an embed preview tool [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 *
 * @property title The title of the embed appears atop the [description] and right below the [author].
 * The url which when the [title] is clicked will be opened. Set this to `null` for no link.
 * @property description The description of the embed appears after the [title] and before any [Field]. The
 * [description] supports standard Discord markdown as well as [markdown\](links).
 * @property thumbnail The thumbnail appears in the upper-right-hand corner of the embed as a smaller image. Set this
 * to `null` for no thumbnail.
 * @property author The author who's name will appear at the very top of the [OutgoingEmbedPacket]. The [Author.imgUrl] will be
 * shown to the left of the [Author.name] (in the very top-left corner of the [OutgoingEmbedPacket]).
 * @property provider TODO Discord refuses to explain what this is
 * @property fields A [List] of all [Field]s in the [OutgoingEmbedPacket] in order of appearance (top -> bottom, left -> right).
 * @property image The [EmbedGraphic] which is shown at the bottom of the embed as a large image.
 * @property video
 * @property color The color of the [OutgoingEmbedPacket]'s left border. Leaving this `null` will result in the default greyish color.
 * @property footer The [Footer] of the embed shown at the very bottom.
 * @property timeStamp The timestamp is shown to the right of the [footer] and is usually used to mark when the embed
 * was sent, but can be set to any [DateTimeTz].
 */
data class Embed internal constructor(
    val author: Author? = null,
    val title: Title? = null,
    val description: String? = null,
    val fields: List<Field> = emptyList(),
    val color: Color? = null,
    val image: EmbedGraphic? = null,
    val thumbnail: EmbedGraphic? = null,
    val video: EmbedGraphic? = null,
    val provider: Provider? = null, // No idea what this means
    val footer: Footer? = null,
    val timeStamp: String? = null
) {

    /**
     * @property text The text of the embed appears atop the [description] and right below the [author].
     * @property url The url which when the [title] is clicked will be opened. Set this to `null` for no link.
     */
    @Serializable
    data class Title internal constructor(val text: String, val url: String? = null) { override fun toString() = text }

    @Serializable
    data class Author(
        val name: String? = null,
        val url: String? = null,
        val imgUrl: String? = null,
        val proxyImgUrl: String? = null
    )

    @Serializable
    data class Provider(val name: String? = null, val url: String? = null)

    /**
     * A [Field] is a titled paragraph displayed, in order, under the [description].
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
        val proxyUrl: String? = null,
        val height: Short? = null,
        val width: Short? = null
    )

    @Serializable
    data class Footer(val text: String?, val iconUrl: String? = null, val proxyIconUrl: String? = null)

}

internal fun EmbedPacket.toEmbed(): Embed = TODO()
