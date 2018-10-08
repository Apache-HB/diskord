package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.MessageDeletedEvent
import com.serebit.diskord.events.MessageUpdatedEvent
import com.serebit.diskord.internal.packets.MessagePacket
import com.serebit.diskord.internal.payloads.DispatchPayload

internal class MessageCreate(override val s: Int, override val d: MessagePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)
}

internal class MessageUpdate(override val s: Int, override val d: MessagePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageUpdatedEvent(context, d)
}

internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageDeletedEvent(context, d)

    data class Data(val id: Long, val channel_id: Long)
}
