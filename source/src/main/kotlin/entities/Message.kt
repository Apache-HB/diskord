package com.serebit.diskord.entities

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.DeleteMessage
import com.serebit.diskord.internal.network.endpoints.EditMessage
import com.serebit.diskord.internal.packets.MessagePacket
import java.time.Instant
import java.time.OffsetDateTime

/**
 * An object representing a text message sent in a Discord channel.
 */
class Message internal constructor(packet: MessagePacket) : Entity {
    /**
     * The message's unique ID.
     */
    override val id: Long = packet.id
    /** The channel this message was sent from.
     *
     * @throws EntityNotFoundException if the channel does not exist.
     */
    val channel: TextChannel = TextChannel.find(packet.channel_id)
        ?: throw EntityNotFoundException("No channel with ID ${packet.channel_id} found.")
    /**
     * The time at which this message was created.
     */
    val createdAt: Instant = OffsetDateTime.parse(packet.timestamp).toInstant()
    /**
     * The message's content as a String, excluding attachments and embeds.
     */
    var content: String = packet.content
        private set
    /**
     * The time at which this message was last edited. If the message has never been edited, this will be null.
     */
    var editedAt: Instant? = packet.edited_timestamp?.let { OffsetDateTime.parse(it).toInstant() }
        private set
    /**
     * An unordered list of users that this message contains mentions for.
     */
    var userMentions: Set<User> = packet.mentions
        private set
    /**
     * An unordered list of roles that this message contains mentions for.
     */
    var roleMentions: Set<Role> = packet.mention_roles
        private set
    /**
     * Whether or not the message mentions everyone. Only returns true if the user who sent the message has
     * permission to ping everyone.
     */
    var mentionsEveryone: Boolean = packet.mention_everyone
        private set
    /**
     * Whether or not the message is currently pinned.
     */
    var isPinned: Boolean = packet.pinned
        private set
    /**
     * Whether or not the message was sent with text-to-speech enabled.
     */
    var isTextToSpeech = packet.tts
        private set

    init {
        EntityCache.cache(this)
    }

    fun reply(text: String) = channel.send(text)

    fun edit(text: String) = Requester.requestObject(EditMessage(channel.id, id), data = mapOf("content" to text))

    fun delete(): Boolean = Requester.requestResponse(DeleteMessage(channel.id, id)).status.successful

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
