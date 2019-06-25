package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.Event
import com.serebit.strife.events.PresenceUpdateEvent
import com.serebit.strife.events.ReadyEvent
import com.serebit.strife.events.ResumedEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.PresencePacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket
import com.serebit.strife.internal.packets.UserPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
internal class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<ReadyEvent, KType>? {
        // assign the context's selfUserID to the given ID before the event is converted
        context.selfUserID = d.user.id

        val user = context.cache.pullUserData(d.user).lazyEntity
        val dmChannels = d.private_channels.map { context.cache.pullDmChannelData(it).lazyEntity }

        return ReadyEvent(context, user, dmChannels) to typeOf<ReadyEvent>()
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
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<ResumedEvent, KType> =
        ResumedEvent(context) to typeOf<ResumedEvent>()

    @Serializable
    data class Data(val _trace: List<String>)
}

@Serializable
internal class PresenceUpdate(override val s: Int, override val d: PresencePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<PresenceUpdateEvent, KType>? {
        val guildData = obtainGuildData(context, d.guild_id!!) ?: return null
        val memberData = guildData.members[d.user.id]?.apply { update(d) } ?: return null

        val userData = context.cache.getUserData(d.user.id)
            ?: context.requester.sendRequest(Route.GetUser(d.user.id)).value?.let { context.cache.pullUserData(it) }
            ?: return null

        userData.updateStatus(d)

        return PresenceUpdateEvent(
            context,
            guildData.lazyEntity,
            memberData.toMember(),
            memberData.activity,
            memberData.user.status!!
        ) to typeOf<PresenceUpdateEvent>()
    }
}

@Serializable
internal class Unknown(override val s: Int, val t: String) : DispatchPayload() {
    @Transient
    override val d = 0

    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<Event, KType>? = null
}
