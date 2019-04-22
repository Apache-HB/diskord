package com.serebit.strife.entities

import com.serebit.strife.data.Permission
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.network.Route
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess


/**
 * Represents a Guild (aka "server"), or a self-contained community of users. Guilds contain their own
 * [text][GuildTextChannel] and [voice][GuildVoiceChannel] channels, and can be customized further with
 * [roles][Role] to segment members into different subgroups.
 *
 * @constructor Create a [Guild] instance from an internal [GuildData] instance
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val context = data.context
    override val id get() = data.id

    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of [User.username], and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name get() = data.name
    /** TODO JoinedAt DOCS */
    val joinedAt get() = data.joinedAt

    /** The [User] which owns this [Guild] as a [GuildMember]. */
    val owner get() = data.owner.toMember()
    /** All [members][GuildMember] of this [Guild]. */
    val members get() = data.members.map { it.value.toMember() }
    /** All [roles][Role] of this [Guild]. */
    val roles get() = data.roles.map { it.value.toEntity() }

    /** A [List] of all [GuildChannels][GuildChannel] in this [Guild]. */
    val channels get() = data.allChannels.map { it.value.toEntity() }
    /** A [List] of all [TextChannels][GuildTextChannel] in this [Guild]. */
    val textChannels get() = channels.filterIsInstance<GuildTextChannel>()
    /** A [List] of all [VoiceChannels][GuildVoiceChannel] in this [Guild]. */
    val voiceChannels get() = channels.filterIsInstance<GuildVoiceChannel>()
    /** A [List] of all [channel categories][GuildChannelCategory] in this [Guild]. */
    val channelCategories get() = channels.filterIsInstance<GuildChannelCategory>()
    /** The [TextChannel][GuildTextChannel] to which system messages are sent. TODO more specific docs*/
    val systemChannel get() = data.systemChannel?.toEntity()
    /** The [GuildChannel] for the server widget. TODO more specific docs */
    val widgetChannel get() = data.widgetChannel?.toEntity()
    /** The [GuildVoiceChannel] to which AFK members are sent to after not speaking for [afkTimeout] seconds. */
    val afkChannel get() = data.afkChannel?.toEntity()
    /** The [GuildVoiceChannel] AFK timeout in seconds. */
    val afkTimeout get() = data.afkTimeout

    /** [Permissions][Permission] for the client in the [Guild] (not including channel overrides). */
    val permissions get() = data.permissions

    /**
     * Whether [members][GuildMember] who have not explicitly set their notification settings will receive
     * a notification for every [message][Message] in this [Guild]. (`ALL` or Only `@Mentions`)
     */
    val defaultMessageNotifications get() = data.defaultMessageNotifications
    /** How broadly, if at all, should Discord automatically filter [messages][Message] for explicit content. */
    val explicitContentFilter get() = data.explicitContentFilter
    val enabledFeatures get() = data.features
    /** The [VerificationLevel] required for the [Guild]. */
    val verificationLevel get() = data.verificationLevel
    /** The [Multi-Factor Authentication Level][MfaLevel] required to send [messages][Message] in this [Guild]. */
    val mfaLevel get() = data.mfaLevel
    /** Is this [Guild] embeddable (e.g. widget). */
    val isEmbedEnabled get() = data.isEmbedEnabled
    /** The [Channel] that the widget will generate an invite to. */
    val embedChannel get() = data.embedChannel?.toEntity()

    val icon: String? get() = data.iconHash
    val splashImage: String? get() = data.splashHash
    val region: String get() = data.region
    /** whether this is considered a "large" [Guild] by Discord. */
    val isLarge: Boolean get() = data.isLarge


    /**
     * Kick a [GuildMember] from this [Guild]. This requires [Permission.KickMembers].
     * Returns `true` if the [GuildMember] was successful kicked from the [Guild]
     */
    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(Route.KickMember(id, user.id)).status.isSuccess()

    /**
     * Ban a [GuildMember] from this [Guild] and delete their messages from all [text channels][TextChannel]
     * from the past [deleteMessageDays] days ``(0-7)``. This requires [Permission.BanMembers].
     * @return `true` if the [GuildMember] was successful banned from the [Guild]
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(Route.BanMember(id, user.id, deleteMessageDays, reason)).status.isSuccess()

    companion object {
        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 32
        val NAME_LENGTH_RANGE = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

/**
 * A [GuildMember] is a [User] associated with a specific [Guild (aka server)][Guild]. A [GuildMember] holds
 * data about the encased [User] which exists only in the respective [Guild].
 *
 * @constructor Builds a [GuildMember] object from data within a [GuildMemberData].
 */
class GuildMember internal constructor(private val data: GuildMemberData) {
    /** The backing [User] of this [GuildMember]. */
    val user get() = data.user.toEntity()
    /** The [Guild] in which this [GuildMember] resides. */
    val guild get() = data.guild.toEntity()
    /** The [Roles][Role] this [GuildMember] is in. */
    val roles get() = data.roles.map { it.toEntity() }
    /** An optional [nickname] which is used as an alias for the [User] in the [Guild]. */
    val nickname get() = data.nickname
    /** The [DateTimeTz] when the [user] joined the [guild]. */
    val joinedAt get() = data.joinedAt
    /** Whether the [GuildMember] is deafened in [Voice Channels][GuildVoiceChannel]. */
    val isDeafened get() = data.isDeafened
    /** Whether the [GuildMember] is muted in [Voice Channels][GuildVoiceChannel]. */
    val isMuted get() = data.isMuted

    override fun equals(other: Any?) = other is GuildMember && other.user == user && other.guild == guild
}

/**
 * Whether [members][GuildMember] who have not explicitly set their notification settings will receive
 * a notification for every [message][Message] in this [Guild].
 */
enum class MessageNotificationLevel { ALL_MESSAGES, ONLY_MENTIONS }

/** How broadly, if at all, should Discord automatically filter [messages][Message] for explicit content. */
enum class ExplicitContentFilterLevel { DISABLED, MEMBERS_WITHOUT_ROLES, ALL_MEMBERS }

/** Multi-factor Authentication level of a [Guild]. */
enum class MfaLevel { NONE, ELEVATED }

/**
 * The verification criteria needed for users to send a [Message] either within a [Guild]
 * or directly to any [GuildMember] in a [Guild].
 */
enum class VerificationLevel { NONE, LOW, MEDIUM, HIGH, VERY_HIGH }
