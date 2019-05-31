package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable

/** Attempt to get [GuildData] from [cache][BotClient.cache], else attempt to request data. */
internal suspend fun obtainGuildData(context: BotClient, id: Long) = context.cache.getGuildData(id)
    ?: context.requester.sendRequest(Route.GetGuild(id)).value?.let { context.cache.pushGuildData(it) }

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        GuildCreateEvent(context, guild = context.cache.pushGuildData(d).toEntity())
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        GuildUpdateEvent(context, guild = context.cache.pullGuildData(d).toEntity())
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): GuildDeleteEvent {
        context.cache.decache(d.id)

        return GuildDeleteEvent(context, guildID = d.id, wasKicked = d.unavailable == null)
    }
}

@Serializable
internal class GuildBanAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): GuildBanAddEvent? {
        val guild = context.cache.getGuildData(d.guild_id)?.toEntity() ?: return null
        val user = context.cache.pullUserData(d.user).toEntity()

        return GuildBanAddEvent(context, guild, user)
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildBanRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): GuildBanRemoveEvent? {
        val guild = context.cache.getGuildData(d.guild_id)?.toEntity() ?: return null
        val user = context.cache.pullUserData(d.user).toEntity()

        return GuildBanRemoveEvent(context, guild, user)
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberAdd(override val s: Int, override val d: GuildMemberPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): GuildMemberJoinEvent? {
        val guildData = d.guild_id?.let { context.cache.getGuildData(d.guild_id) } ?: return null
        val member = d.toData(guildData, context).also { guildData.members[it.user.id] = it }

        return GuildMemberJoinEvent(context, guildData.toEntity(), member.toMember())
    }
}

@Serializable
internal class GuildMemberRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): GuildMemberLeaveEvent? {
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val user = context.cache.pullUserData(d.user).toEntity()
        guildData.members -= d.user.id

        return GuildMemberLeaveEvent(context, guildData.toEntity(), user)
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): Event? {
        context.cache.pullUserData(d.user)
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val member = guildData.members[d.user.id]?.also { it.update(d.roles, d.nick) } ?: return null

        return GuildMemberUpdateEvent(context, guildData.toEntity(), member.toMember())
    }

    @Serializable
    data class Data(val guild_id: Long, val roles: List<Long>, val user: UserPacket, val nick: String? = null)
}
