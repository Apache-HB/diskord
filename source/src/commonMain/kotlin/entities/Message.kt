package com.serebit.diskord.entities

import com.serebit.diskord.Context
import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.endpoints.DeleteMessage
import com.serebit.diskord.internal.network.endpoints.EditMessage
import com.serebit.diskord.internal.network.endpoints.GetMessage

/**
 * Represents a text message sent in a Discord text channel.
 */
class Message internal constructor(
    override val id: Long,
    private val channelId: Long,
    override val context: Context
) : Entity {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: context.requester.requestObject(GetMessage(channelId, id))
            ?: throw EntityNotFoundException("Invalid message instantiated with ID $id.")
    /**
     * The author of this message as a User.
     */
    val author: User get() = User(packet.author.id, context)
    /**
     * The text channel this message was sent to.
     */
    val channel: TextChannel get() = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")
    /**
     * The message's text content, excluding attachments and embeds.
     */
    val content: String get() = packet.content
    /**
     * The time at which this message was last edited. If the message has never been edited, this will be null.
     */
    val editedAt: DateTime? get() = packet.editedTimestamp
    /**
     * An ordered list of users that this message contains mentions for.
     */
    val userMentions: List<User> get() = packet.mentions.map { User(it.id, context) }
    /**
     * An ordered list of roles that this message contains mentions for.
     */
    val roleMentions: List<Role> get() = packet.mention_roles.map { Role(it, context) }
    /**
     * Whether or not the message mentions everyone. Only returns true if the user who sent the message has
     * permission to ping everyone.
     */
    val mentionsEveryone: Boolean get() = packet.mention_everyone
    /**
     * Whether or not the message is currently pinned.
     */
    val isPinned: Boolean get() = packet.pinned
    /**
     * Whether or not the message was sent with text-to-speech enabled.
     */
    val isTextToSpeech get() = packet.tts

    fun reply(text: String) = channel.send(text)

    fun edit(text: String) = also {
        context.requester.requestObject(EditMessage(channel.id, id), data = mapOf("content" to text))
    }

    fun delete(): Boolean = context.requester.sendRequest(DeleteMessage(channel.id, id))

    operator fun contains(text: String) = text in content

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
