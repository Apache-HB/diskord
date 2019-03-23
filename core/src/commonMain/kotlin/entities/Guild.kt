package com.serebit.strife.entities

import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.network.GuildRoute
import io.ktor.http.isSuccess

/**
 * Represents a Guild (aka "server"), or a self-contained community of users. Guilds contain their own
 * [text][GuildTextChannel] and [voice][GuildVoiceChannel] channels, and can be customized further with [roles][Role]
 * to segment members into different subgroups.
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val id = data.id
    override val context = data.context
    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of [User.username], and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = data.name
    val joinedAt get() = data.joinedAt
    val channels get() = data.allChannels.map { it.value.toEntity() }
    val textChannels get() = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels get() = channels.filterIsInstance<GuildVoiceChannel>()
    val channelCategories get() = channels.filterIsInstance<GuildChannelCategory>()
    val afkChannel get() = data.afkChannel?.toEntity()
    val systemChannel get() = data.systemChannel?.toEntity()
    val widgetChannel get() = data.widgetChannel?.toEntity()
    val afkTimeout get() = data.afkTimeout
    val members get() = data.members.map { it.value.toMember() }
    val roles get() = data.roles.map { it.value.toEntity() }
    val owner get() = data.owner.toMember()
    val permissions get() = data.permissions
    val defaultMessageNotifications get() = data.defaultMessageNotifications
    val explicitContentFilter get() = data.explicitContentFilter
    val enabledFeatures get() = data.features
    val verificationLevel get() = data.verificationLevel
    val mfaLevel get() = data.mfaLevel
    val isEmbedEnabled get() = data.isEmbedEnabled
    val embedChannel get() = data.embedChannel?.toEntity()
    val icon: String? get() = data.iconHash
    val splashImage: String? get() = data.splashHash
    val region: String get() = data.region
    val isLarge: Boolean get() = data.isLarge

    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(GuildRoute.KickMember(id, user.id)).status.isSuccess()

    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(GuildRoute.BanMember(id, user.id, deleteMessageDays, reason)).status.isSuccess()

    companion object {
        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 32
        val NAME_LENGTH_RANGE = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

class GuildMember internal constructor(private val data: GuildMemberData) {
    val user get() = data.user.toEntity()
    val guild get() = data.guild.toEntity()
    val roles get() = data.roles.map { it.toEntity() }
    val nickname get() = data.nickname
    val joinedAt get() = data.joinedAt
    val isDeafened get() = data.isDeafened
    val isMuted get() = data.isMuted

    override fun equals(other: Any?) = other is GuildMember && other.user == user && other.guild == guild
}

enum class MessageNotificationLevel { ALL_MESSAGES, ONLY_MENTIONS }

enum class ExplicitContentFilterLevel { DISABLED, MEMBERS_WITHOUT_ROLES, ALL_MEMBERS }

enum class MfaLevel { NONE, ELEVATED }

enum class VerificationLevel { NONE, LOW, MEDIUM, HIGH, VERY_HIGH }
