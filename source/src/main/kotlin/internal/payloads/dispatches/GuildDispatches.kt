package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.payloads.DispatchPayload

internal class GuildCreate(override val s: Int, override val d: GuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildCreatedEvent(context, d)
}
