package com.serebit.diskord.entities

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.cacheAll
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.DeleteMessage
import com.serebit.diskord.internal.network.endpoints.EditMessage
import com.serebit.diskord.internal.packets.MessagePacket
import io.ktor.http.isSuccess

/**
 * Represents a text message sent in a Discord text channel.
 */
class Message internal constructor(packet: MessagePacket) : Entity {
    override val id: Long = packet.id
    /** The channel this message was sent from.
     *
     * @throws EntityNotFoundException if the channel does not exist.
     */
    val channel: TextChannel = TextChannel.find(packet.channel_id)
        ?: throw EntityNotFoundException("No channel with ID ${packet.channel_id} found.")
    /**
     * The message's text content, excluding attachments and embeds.
     */
    val content: String = packet.content
    /**
     * The time at which this message was last edited. If the message has never been edited, this will be null.
     */
    val editedAt: DateTime? = packet.edited_timestamp?.let { DateTime.fromIsoTimestamp(it) }
    /**
     * An ordered list of users that this message contains mentions for.
     */
    val userMentions: List<User> = packet.mentions.map(::User).cacheAll()
    /**
     * An ordered list of roles that this message contains mentions for.
     */
    val roleMentions: List<Role> = packet.mention_roles.map(::Role).cacheAll()
    /**
     * Whether or not the message mentions everyone. Only returns true if the user who sent the message has
     * permission to ping everyone.
     */
    val mentionsEveryone: Boolean = packet.mention_everyone
    /**
     * Whether or not the message is currently pinned.
     */
    val isPinned: Boolean = packet.pinned
    /**
     * Whether or not the message was sent with text-to-speech enabled.
     */
    val isTextToSpeech = packet.tts

    fun reply(text: String) = channel.send(text)

    fun edit(text: String) = Requester.requestObject(EditMessage(channel.id, id), mapOf(), mapOf("content" to text))
        ?.let { Message(it).cache() }

    fun delete(): Boolean = Requester.requestResponse(DeleteMessage(channel.id, id)).status.isSuccess()

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
