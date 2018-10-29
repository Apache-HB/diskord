package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.MessagePacket
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet.id, packet.channel_id).cache()
    val guild: Guild? by lazy {
        EntityCache.find<Guild> { packet.channel_id in it.textChannels.map(GuildTextChannel::id) }
    }
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet.id, packet.channel.id)
    val guild: Guild? by lazy {
        EntityCache.find<Guild> { packet.channel_id in it.textChannels.map(GuildTextChannel::id) }
    }
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = TextChannel.find(packet.channel_id)
        ?: throw EntityNotFoundException("No channel with ID ${packet.channel_id} found.")
    val guild: Guild? by lazy {
        EntityCache.find<Guild> { packet.channel_id in it.textChannels.map(GuildTextChannel::id) }
    }
}
