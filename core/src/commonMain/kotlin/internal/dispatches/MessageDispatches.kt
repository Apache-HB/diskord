package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.MessageDeletedEvent
import com.serebit.strife.events.MessageUpdatedEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import kotlinx.serialization.Serializable

@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageUpdatedEvent(context, d)
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = MessageDeletedEvent(context, d)

    @Serializable
    data class Data(val id: Long, val channel_id: Long)
}
