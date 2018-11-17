package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.ReadyEvent
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket
import com.serebit.diskord.internal.packets.UserPacket
import com.serebit.diskord.internal.payloads.DispatchPayload
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ReadyEvent(context, d)

    @Serializable
    data class Data(
        val v: Int,
        val user: UserPacket,
        val private_channels: List<DmChannelPacket>,
        val guilds: List<UnavailableGuildPacket>,
        val _trace: List<String>
    )
}

@Serializable
internal class Unknown(override val s: Int, val t: String) : DispatchPayload() {
    @Transient
    override val d = 0

    override suspend fun asEvent(context: Context): Event? = null
}
