package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.Event
import com.serebit.strife.events.ReadyEvent
import com.serebit.strife.events.ResumeEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket
import com.serebit.strife.internal.packets.UserPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ReadyEvent> {
        // assign the context's selfUserID to the given ID before the event is converted
        context.selfUserID = d.user.id

        d.guilds.forEach { context.cache.initGuildData(it.id) }

        val user = context.cache.pullUserData(d.user).lazyEntity
        val dmChannels = d.private_channels.map { context.cache.pullDmChannelData(it).lazyEntity }

        return success(ReadyEvent(context, user, dmChannels))
    }

    @Serializable
    data class Data(
        val v: Int,
        val user: UserPacket,
        val private_channels: List<DmChannelPacket>,
        val guilds: List<UnavailableGuildPacket>,
        val session_id: String,
        val _trace: List<String>,
        val shard: List<Int>? = null
    )
}

@Serializable
internal class Resumed(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ResumeEvent> =
        success(ResumeEvent(context))

    @Serializable
    data class Data(val _trace: List<String>)
}

@Serializable
internal class Unknown(override val s: Int, val t: String) : DispatchPayload() {
    @Transient
    override val d = 0

    override suspend fun asEvent(context: BotClient): DispatchConversionResult<Event> =
        failure("Received unknown dispatch type $t")
}
