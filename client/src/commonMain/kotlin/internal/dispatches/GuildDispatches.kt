package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable

@Serializable
internal class GuildCreate(override val s: Int, override val d: GuildCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(GuildCreateEvent(context, context.cache.pushGuildData(d).lazyEntity))
}

@Serializable
internal class GuildUpdate(override val s: Int, override val d: GuildUpdatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(GuildUpdateEvent(context, guild = context.cache.pullGuildData(d).lazyEntity))
}

@Serializable
internal class GuildDelete(override val s: Int, override val d: UnavailableGuildPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildDeleteEvent> {
        context.cache.removeGuildData(d.id)

        return success(GuildDeleteEvent(context, guildID = d.id, wasKicked = d.unavailable == null))
    }
}

@Serializable
internal class GuildBanAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildBanAddEvent> {
        val guild = context.cache.getGuildData(d.guild_id)?.lazyEntity
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val user = context.cache.pullUserData(d.user).lazyEntity

        return success(GuildBanAddEvent(context, guild, user))
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildBanRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildBanRemoveEvent> {
        val guild = context.cache.getGuildData(d.guild_id)?.lazyEntity
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val user = context.cache.pullUserData(d.user).lazyEntity

        return success(GuildBanRemoveEvent(context, guild, user))
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildEmojisUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildEmojisUpdateEvent> {
        val guildData = context.cache.getGuildData(d.guild_id)
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val guild = guildData.lazyEntity
        val emojis = d.emojis.map { it.toData(context).lazyEntity }

        return success(GuildEmojisUpdateEvent(context, guild, emojis))
    }

    @Serializable
    data class Data(val guild_id: Long, val emojis: List<GuildEmojiPacket>)
}

@Serializable
internal class GuildMemberAdd(override val s: Int, override val d: GuildMemberPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildMemberJoinEvent> {
        val guildData = d.guild_id?.let { context.cache.getGuildData(d.guild_id) }
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val memberData = d.toData(guildData, context)
        // TODO: GuildData.update(GuildMemberAdd)

        return success(GuildMemberJoinEvent(
            context, guildData.lazyEntity, memberData.lazyMember
        ))
    }
}

@Serializable
internal class GuildMemberRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildMemberLeaveEvent> {
        val guildData = context.cache.getGuildData(d.guild_id)
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val user = context.cache.pullUserData(d.user).lazyEntity
        // TODO: GuildData.update(GuildMemberRemove)

        return success(GuildMemberLeaveEvent(context, guildData.lazyEntity, user))
    }

    @Serializable
    data class Data(val guild_id: Long, val user: UserPacket)
}

@Serializable
internal class GuildMemberUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<GuildMemberUpdateEvent> {
        context.cache.pullUserData(d.user)
        val guildData = context.cache.getGuildData(d.guild_id)
            ?: return failure("Failed to get guild with id ${d.guild_id} from cache")

        val member = guildData.getMemberData(d.user.id)?.also { it.update(d.roles, d.nick) }
            ?: return failure("Failed to get member with ID ${d.user.id} from guild with ID ${d.guild_id}")

        return success(GuildMemberUpdateEvent(context, guildData.lazyEntity, member.lazyMember))
    }

    @Serializable
    data class Data(val guild_id: Long, val roles: List<Long>, val user: UserPacket, val nick: String? = null)
}
