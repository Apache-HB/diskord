package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.events.Event
import com.serebit.strife.events.ReadyEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket
import com.serebit.strife.internal.packets.UserPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): ReadyEvent? {
        val user = context.cache.pullUserData(d.user).toEntity()
        val dmChannels = d.private_channels.mapNotNull {
            context.cache.pushChannelData(it).toEntity() as? DmChannel
        }

        return ReadyEvent(context, user, dmChannels)
    }

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
