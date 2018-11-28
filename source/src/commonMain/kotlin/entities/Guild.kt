package com.serebit.diskord.entities

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.data.Member
import com.serebit.diskord.entities.channels.ChannelCategory
import com.serebit.diskord.entities.channels.GuildChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.network.endpoints.BanGuildMember
import com.serebit.diskord.internal.network.endpoints.GetGuild
import com.serebit.diskord.internal.network.endpoints.KickGuildMember

/**
 * Represents a Discord guild (aka "server"), or a self-contained community of users. Guilds contain their own text
 * and voice channels, and can be customized further with [roles][Role] to segment members into different subgroups.
 */
class Guild internal constructor(override val id: Long, override val context: Context) : Entity {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: context.requester.requestObject(GetGuild(id))
            ?: throw EntityNotFoundException("Invalid guild instantiated with ID $id.")
    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of usernames, and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = packet.name
    val joinedAt get() = packet.joinedAt
    val channels get() = packet.typedChannels.map { GuildChannel.from(it, context) }
    val textChannels get() = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels get() = channels.filterIsInstance<GuildVoiceChannel>()
    val channelCategories get() = channels.filterIsInstance<ChannelCategory>()
    val afkChannel get() = channels.filterIsInstance<GuildVoiceChannel>().find { it.id == packet.afk_channel_id }
    val systemChannel get() = channels.filterIsInstance<GuildTextChannel>().find { it.id == packet.system_channel_id }
    val widgetChannel get() = channels.filterIsInstance<GuildTextChannel>().find { it.id == packet.widget_channel_id }
    val afkTimeout get() = packet.afk_timeout
    val members get() = packet.members.map { Member(it, context) }
    val roles get() = packet.roles.map { Role(it.id, context) }
    val owner get() = packet.members.map { Member(it, context) }.find { it.user.id == packet.owner_id }
    val permissions get() = packet.permissionsList
    val defaultMessageNotifications get() = packet.default_message_notifications
    val explicitContentFilter get() = packet.explicit_content_filter
    val enabledFeatures get() = packet.features
    val verificationLevel get() = packet.verification_level
    val mfaLevel get() = packet.mfa_level
    val isEmbedEnabled get() = packet.embed_enabled
    val embedChannel get() = channels.filterIsInstance<GuildTextChannel>().find { it.id == packet.embed_channel_id }
    val icon: String? get() = packet.icon
    val splashImage: String? get() = packet.splash
    val region: String get() = packet.region
    val isLarge: Boolean get() = packet.large

    fun kick(user: User): Boolean = context.requester.sendRequest(KickGuildMember(id, user.id))

    fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(
            BanGuildMember(id, user.id), mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        )

    companion object {
        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 32
        val NAME_LENGTH_RANGE = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

internal fun GuildData.toGuild() = Guild(id, context)
