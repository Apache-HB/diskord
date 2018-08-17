package com.serebit.diskord.entities

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetChannel
import com.serebit.diskord.internal.packets.MessagePacket
import java.time.OffsetDateTime

class Message internal constructor(packet: MessagePacket) : Entity {
    override val id: Long = packet.id
    val channel: TextChannel = EntityCache.find(packet.channel_id)
        ?: Requester.requestObject(GetChannel(packet.channel_id)) as? TextChannel
        ?: throw EntityNotFoundException("No channel with ID ${packet.channel_id} found.")
    val createdAt: OffsetDateTime = OffsetDateTime.parse(packet.timestamp)
    var content: String = packet.content
        private set
    var editedAt: OffsetDateTime? = packet.edited_timestamp?.let { OffsetDateTime.parse(it) }
        private set
    var userMentions: List<User> = packet.mentions
        private set
    var roleMentions: List<Role> = packet.mention_roles
        private set
    var mentionsEveryone: Boolean = packet.mention_everyone
        private set
    var isPinned: Boolean = packet.pinned
        private set
    var isTextToSpeech = packet.tts
        private set

    init {
        EntityCache.cache(this)
    }

    fun reply(text: String) = channel.send(text)

    enum class MessageType(val value: Int) {
        DEFAULT(0),
        RECIPIENT_ADD(1), RECIPIENT_REMOVE(2),
        CALL(3),
        CHANNEL_NAME_CHANGE(4), CHANNEL_ICON_CHANGE(5), CHANNEL_PINNED_MESSAGE(6),
        GUILD_MEMBER_JOIN(7)
    }
}
