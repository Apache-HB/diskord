package com.serebit.strife.entities

import com.serebit.strife.data.Member
import com.serebit.strife.entities.channels.GuildChannelCategory
import com.serebit.strife.entities.channels.GuildTextChannel
import com.serebit.strife.entities.channels.GuildVoiceChannel
import com.serebit.strife.entities.channels.toChannel
import com.serebit.strife.entities.channels.toGuildChannel
import com.serebit.strife.entities.channels.toGuildVoiceChannel
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.network.Endpoint
import io.ktor.http.isSuccess

/**
 * Represents a Discord guild (aka "server"), or a self-contained community of users. Guilds contain their own text
 * and voice channels, and can be customized further with [roles][Role] to segment members into different subgroups.
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val id = data.id
    override val context = data.context
    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of usernames, and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = data.name
    val joinedAt get() = data.joinedAt
    val channels get() = data.allChannels.map { it.value.toChannel() }
    val textChannels get() = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels get() = channels.filterIsInstance<GuildVoiceChannel>()
    val channelCategories get() = channels.filterIsInstance<GuildChannelCategory>()
    val afkChannel get() = data.afkChannel?.toGuildVoiceChannel()
    val systemChannel get() = data.systemChannel?.toGuildChannel()
    val widgetChannel get() = data.widgetChannel?.toGuildChannel()
    val afkTimeout get() = data.afkTimeout
    val members get() = data.members.map { Member(it, context) }
    val roles get() = data.roles.map { it.toRole() }
    val owner get() = Member(data.owner, context)
    val permissions get() = data.permissions
    val defaultMessageNotifications get() = data.defaultMessageNotifications
    val explicitContentFilter get() = data.explicitContentFilter
    val enabledFeatures get() = data.features
    val verificationLevel get() = data.verificationLevel
    val mfaLevel get() = data.mfaLevel
    val isEmbedEnabled get() = data.isEmbedEnabled
    val embedChannel get() = data.embedChannel?.toGuildChannel()
    val icon: String? get() = data.iconHash
    val splashImage: String? get() = data.splashHash
    val region: String get() = data.region
    val isLarge: Boolean get() = data.isLarge

    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(Endpoint.KickGuildMember(id, user.id)).status.isSuccess()

    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(
            Endpoint.BanGuildMember(id, user.id), mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        ).status.isSuccess()

    companion object {
        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 32
        val NAME_LENGTH_RANGE = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

internal fun GuildData.toGuild() = Guild(this)
