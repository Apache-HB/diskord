package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.packets.DmChannelPacket

class DmChannel internal constructor(packet: DmChannelPacket) : TextChannel {
    override val id = packet.id
    val lastMessage: Message? = packet.last_message_id?.let { EntityCache.find(id) }
    var recipients: List<User> = packet.recipients.map { User(it) }
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 1
    }
}
