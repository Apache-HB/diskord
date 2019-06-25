package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.entities.DmChannel
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

@Serializable
internal class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ReadyEvent> {
        // assign the context's selfUserID to the given ID before the event is converted
        context.selfUserID = d.user.id

        val user = context.cache.pullUserData(d.user).lazyEntity
        val dmChannels = d.private_channels.mapNotNull {
            context.cache.pushChannelData(it).lazyEntity as? DmChannel
        }

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
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ResumedEvent> =
        success(ResumedEvent(context))

    @Serializable
    data class Data(val _trace: List<String>)
}

@Serializable
internal class PresenceUpdate(override val s: Int, override val d: PresencePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<PresenceUpdateEvent> {
        val guildData = context.cache.getGuildData(d.guild_id!!)
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val memberData = guildData.members[d.user.id]?.apply { update(d) }
            ?: return failure("Failed to get member with ID ${d.user.id} from guild with ID ${d.guild_id}")

        val userData = context.cache.getUserData(d.user.id)
            ?: context.requester.sendRequest(Route.GetUser(d.user.id)).value?.let { context.cache.pullUserData(it) }
            ?: return failure("Failed to get user with ID ${d.user.id}")

        userData.updateStatus(d)

        return success(
            PresenceUpdateEvent(
                context,
                guildData.lazyEntity,
                memberData.toMember(),
                memberData.activity,
                memberData.user.status!!
            )
        )
    }
}

@Serializable
internal class Unknown(override val s: Int, val t: String) : DispatchPayload() {
    @Transient
    override val d = 0

    override suspend fun asEvent(context: BotClient): DispatchConversionResult<Event> =
        failure("Received unknown dispatch type $t")
}
