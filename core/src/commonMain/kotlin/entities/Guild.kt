package com.serebit.strife.entities

import com.serebit.strife.data.Member
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.channels.GuildChannelCategory
import com.serebit.strife.entities.channels.GuildTextChannel
import com.serebit.strife.entities.channels.GuildVoiceChannel
import com.serebit.strife.entities.channels.TextChannel
import com.serebit.strife.entities.channels.toChannel
import com.serebit.strife.entities.channels.toGuildChannel
import com.serebit.strife.entities.channels.toGuildVoiceChannel
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.network.Endpoint.BanGuildMember
import com.serebit.strife.internal.network.Endpoint.KickGuildMember
import io.ktor.http.isSuccess


/**
 * Represents a Guild (aka "server"), or a self-contained community of users. [Guilds][Guild]
 * contain their own [text][TextChannel] and [voice][GuildVoiceChannel] channels, and can
 * be customized further with [roles][Role] to segment members into different subgroups.
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val id = data.id
    override val context = data.context
    /**
     * The name of a Guild is not unique across Discord, and as such, any two
     * guilds can have the same name. Guild names are subject to similar
     * restrictions as those of [User.username], and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width
     * and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = data.name
    val joinedAt get() = data.joinedAt
    val channels get() = data.allChannels.map { it.value.toChannel() }
    val textChannels get() = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels get() = channels.filterIsInstance<GuildVoiceChannel>()
    val channelCategories get() = channels.filterIsInstance<GuildChannelCategory>()
    val systemChannel get() = data.systemChannel?.toGuildChannel()
    val widgetChannel get() = data.widgetChannel?.toGuildChannel()
    val afkChannel get() = data.afkChannel?.toGuildVoiceChannel()
    val afkTimeout get() = data.afkTimeout
    val members get() = data.members
    val roles get() = data.roles.map { it.value.toRole() }
    /** The [User] which owns this [Guild] as a [Member]. */
    val owner get() = data.owner
    /** [permissions][Permission] for the client in the [Guild] (not including channel overrides). */
    val permissions get() = data.permissions
    /** Default Message Notification Level (ALL or MENTIONS). */
    val defaultNotification get() = data.defaultNotificationLevel
    val explicitContentFilter get() = data.explicitContentFilter
    val enabledFeatures get() = data.features
    val verificationLevel get() = data.verificationLevel
    val mfaLevel get() = data.mfaLevel
    val isEmbedEnabled get() = data.isEmbedEnabled
    val embedChannel get() = data.embedChannel?.toGuildChannel()
    val icon: String? get() = data.iconHash
    val splashImage: String? get() = data.splashHash
    val region: String get() = data.region
    /** whether this is considered a "large" [Guild] by Discord. */
    val isLarge: Boolean get() = data.isLarge

    /**
     * Kick a [Member] from this [Guild]. This requires [Permission.KickMembers].
     * Returns `true` if the [Member] was successful kicked from the [Guild]
     */
    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(KickGuildMember(id, user.id)).status.isSuccess()

    /**
     * Ban a [Member] from this [Guild] and delete their messages from all [text channels][TextChannel]
     * from the past [deleteMessageDays] days ``(0-7)``. This requires [Permission.BanMembers].
     * @return `true` if the [Member] was successful banned from the [Guild]
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(
            BanGuildMember(id, user.id), mapOf(
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

/** Encapsulate a [GuildData] instance in an end-user facing [Guild] entity. */
internal fun GuildData.toGuild() = Guild(this)

enum class MessageNotificationLevel { ALL_MESSAGES, ONLY_MENTIONS }

enum class ExplicitContentFilterLevel { DISABLED, MEMBERS_WITHOUT_ROLES, ALL_MEMBERS }

/** Multi-factor Authentication level of a [Guild]. */
enum class MfaLevel { NONE, ELEVATED }

/**
 * The verification criteria needed for users to send a [Message] either within a [Guild]
 * or directly to any [Member] in a [Guild].
 */
enum class VerificationLevel { NONE, LOW, MEDIUM, HIGH, VERY_HIGH }
