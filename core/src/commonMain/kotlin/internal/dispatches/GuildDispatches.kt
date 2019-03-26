package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) =
        GuildCreateEvent(context, guild = context.cache.pushGuildData(d).toEntity())
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) =
        GuildUpdateEvent(context, guild = context.cache.pullGuildData(d).toEntity())
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context): GuildDeleteEvent {
        context.cache.decache(d.id)

        return GuildDeleteEvent(context, guildID = d.id, wasKicked = d.unavailable == null)
    }
}

@Serializable
internal class GuildBanAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): GuildBanAddEvent? {
        val guild = context.cache.getGuildData(d.guild_id)?.toEntity() ?: return null
        val user = context.cache.pullUserData(d.user).toEntity()

        return GuildBanAddEvent(context, guild, user)
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildBanRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): GuildBanRemoveEvent? {
        val guild = context.cache.getGuildData(d.guild_id)?.toEntity() ?: return null
        val user = context.cache.pullUserData(d.user).toEntity()

        return GuildBanRemoveEvent(context, guild, user)
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberAdd(override val s: Int, override val d: GuildMemberPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context): GuildMemberJoinEvent? {
        val guildData = d.guild_id?.let { context.cache.getGuildData(d.guild_id) } ?: return null
        val member = d.toData(guildData, context).also { guildData.members[it.user.id] = it }

        return GuildMemberJoinEvent(context, guildData.toEntity(), member.toMember())
    }
}

@Serializable
internal class GuildMemberRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): GuildMemberLeaveEvent? {
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
    override suspend fun asEvent(context: Context): Event? {
        context.cache.pullUserData(d.user)
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val member = guildData.members[d.user.id]?.also { it.update(d.roles, d.nick) } ?: return null

        return GuildMemberUpdateEvent(context, guildData.toEntity(), member.toMember())
    }

    @Serializable
    data class Data(val guild_id: Long, val roles: List<Long>, val user: UserPacket, val nick: String? =  null)
}
