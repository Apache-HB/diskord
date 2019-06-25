package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** Attempt to get [GuildData] from [cache][BotClient.cache], else attempt to request data. */
internal suspend fun obtainGuildData(context: BotClient, id: Long) = context.cache.getGuildData(id)
    ?: context.requester.sendRequest(Route.GetGuild(id)).value?.let { context.cache.pushGuildData(it) }

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) =
        GuildCreateEvent(context, context.cache.pushGuildData(d).lazyEntity) to typeOf<GuildCreateEvent>()
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) =
        GuildUpdateEvent(context, guild = context.cache.pullGuildData(d).lazyEntity) to typeOf<GuildUpdateEvent>()
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildDeleteEvent, KType> {
        context.cache.removeGuildData(d.id)

        return GuildDeleteEvent(
            context, guildID = d.id, wasKicked = d.unavailable == null
        ) to typeOf<GuildDeleteEvent>()
    }
}

@Serializable
internal class GuildBanAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildBanAddEvent, KType>? {
        val guild = context.cache.getGuildData(d.guild_id)?.lazyEntity ?: return null
        val user = context.cache.pullUserData(d.user).lazyEntity

        return GuildBanAddEvent(context, guild, user) to typeOf<GuildBanAddEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildBanRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildBanRemoveEvent, KType>? {
        val guild = context.cache.getGuildData(d.guild_id)?.lazyEntity ?: return null
        val user = context.cache.pullUserData(d.user).lazyEntity

        return GuildBanRemoveEvent(context, guild, user) to typeOf<GuildBanRemoveEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildEmojisUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildEmojisUpdateEvent, KType>? {
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val guild = guildData.lazyEntity
        val emojis = d.emojis.map { it.toData(context).lazyEntity }

        return GuildEmojisUpdateEvent(context, guild, emojis) to typeOf<GuildEmojisUpdateEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long, val emojis: List<GuildEmojiPacket>)
}

@Serializable
internal class GuildMemberAdd(override val s: Int, override val d: GuildMemberPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildMemberJoinEvent, KType>? {
        val guildData = d.guild_id?.let { context.cache.getGuildData(d.guild_id) } ?: return null
        val memberData = d.toData(guildData, context)
        // TODO: GuildData.update(GuildMemberAdd)

        return GuildMemberJoinEvent(
            context, guildData.lazyEntity, memberData.lazyMember
        ) to typeOf<GuildMemberJoinEvent>()
    }
}

@Serializable
internal class GuildMemberRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildMemberLeaveEvent, KType>? {
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val user = context.cache.pullUserData(d.user).lazyEntity
        // TODO: GuildData.update(GuildMemberRemove)

        return GuildMemberLeaveEvent(context, guildData.lazyEntity, user) to typeOf<GuildMemberLeaveEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<GuildMemberUpdateEvent, KType>? {
        context.cache.pullUserData(d.user)
        val guildData = context.cache.getGuildData(d.guild_id) ?: return null
        val memberData = guildData.getMemberData(d.user.id)?.also { it.update(d.roles, d.nick) } ?: return null

        return GuildMemberUpdateEvent(
            context, guildData.lazyEntity, memberData.lazyMember
        ) to typeOf<GuildMemberUpdateEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long, val roles: List<Long>, val user: UserPacket, val nick: String? = null)
}
