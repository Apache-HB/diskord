package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.MessagePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet.id, packet.channel_id)
    val channel = Channel.find(packet.channel_id) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")
    val guild: Guild? by lazy {
        if (channel is GuildTextChannel) {
            EntityPacketCache.filterIsInstance<GuildCreatePacket>().find { channel in it.textChannels }?.let {
                Guild(it.id)
            }
        } else null
    }
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    val message = Message(packet.id, packet.channel_id)
    val channel = Channel.find(packet.channel_id) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")
    val guild: Guild? by lazy {
        if (channel is GuildTextChannel) {
            EntityPacketCache.filterIsInstance<GuildCreatePacket>().find { channel in it.textChannels }?.let {
                Guild(it.id)
            }
        } else null
    }
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = Channel.find(packet.channel_id) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")
    val guild: Guild? by lazy {
        if (channel is GuildTextChannel) {
            EntityPacketCache.filterIsInstance<GuildCreatePacket>().find { channel in it.textChannels }?.let {
                Guild(it.id)
            }
        } else null
    }
}
