package com.serebit.strife.entities

import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.network.MessageRoute
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess

/** Represents a text message sent in a Discord text channel. */
class Message internal constructor(private val data: MessageData) : Entity {
    override val id = data.id
    override val context = data.context
    /**
     * The author of this message as a [User]. If the message was sent by the system and has no user associated with
     * it, this property will be null.
     */
    val author: User? get() = data.author.toEntity()
    /** The text channel this message was sent to. */
    val channel: TextChannel get() = data.channel.toEntity()
    /** The message's text content, excluding attachments and embeds. */
    val content: String get() = data.content
    /** The time at which this message was last edited. If the message has never been edited, this will be null. */
    val editedAt: DateTimeTz? get() = data.editedAt
    /** An ordered list of users that this message contains mentions for. */
    val userMentions: List<User> get() = data.mentionedUsers.map { it.toEntity() }
    /** An ordered list of roles that this message contains mentions for. */
    val roleMentions: List<Role> get() = data.mentionedRoles.map { it.toEntity() }
    /**
     * Whether or not the message mentions everyone. Only returns true if the user who sent the message has
     * permission to ping everyone.
     */
    val mentionsEveryone: Boolean get() = data.mentionsEveryone
    /** Whether or not the message is currently pinned. */
    val isPinned: Boolean get() = data.isPinned
    /** Whether or not the message was sent with text-to-speech enabled. */
    val isTextToSpeech get() = data.isTextToSpeech

    suspend fun edit(text: String) = also {
        context.requester.sendRequest(MessageRoute.Edit(channel.id, id, text))
    }

    suspend fun delete(): Boolean =
        context.requester.sendRequest(MessageRoute.Delete(channel.id, id)).status.isSuccess()

    override fun equals(other: Any?) = other is Message && other.id == id

    enum class MessageType(val value: Int) {
        DEFAULT(0),
        RECIPIENT_ADD(1), RECIPIENT_REMOVE(2),
        CALL(3),
        CHANNEL_NAME_CHANGE(4), CHANNEL_ICON_CHANGE(5), CHANNEL_PINNED_MESSAGE(6),
        GUILD_MEMBER_JOIN(7)
    }

    companion object {
        const val MAX_LENGTH = 2000
    }
}

suspend fun Message.reply(text: String) = channel.send(text)

operator fun Message.contains(text: String) = text in content
