package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Message
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.MessagePacket

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet).cache()
}
