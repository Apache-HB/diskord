package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.MessageDeletedEvent
import com.serebit.diskord.events.MessageUpdatedEvent
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.payloads.DispatchPayload
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
