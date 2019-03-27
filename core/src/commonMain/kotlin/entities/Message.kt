package com.serebit.strife.entities

import com.serebit.strife.StrifeDsl
import com.serebit.strife.data.Permission
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route.DeleteMessage
import com.serebit.strife.internal.network.Route.EditMessage
import com.serebit.strife.internal.packets.MessageEditPacket
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess

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
    val embeds get() = data.embeds.map { it.toEmbed() }

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
        embed = EmbedBuilder().apply(builder)
    }
}

/** Build a [Message] which can be sent using [TextChannel.send] or [Message.reply]. */
@StrifeDsl
fun message(builder: MessageBuilder.() -> Unit) = MessageBuilder().apply(builder)
