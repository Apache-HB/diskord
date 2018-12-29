package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.GuildCreateEvent
import com.serebit.strife.events.GuildDeleteEvent
import com.serebit.strife.events.GuildUpdateEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket
import kotlinx.serialization.Serializable

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildCreateEvent(context, d)
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildUpdateEvent(context, d)
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildDeleteEvent(context, d)
}
