package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.events.GuildCreateEvent
import com.serebit.diskord.events.GuildDeleteEvent
import com.serebit.diskord.events.GuildUpdateEvent
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket
import com.serebit.diskord.internal.payloads.DispatchPayload
import kotlinx.serialization.Serializable

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildCreateEvent(context, d)
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildUpdateEvent(context, d)
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = GuildDeleteEvent(context, d)
}
