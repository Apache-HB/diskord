package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Permission
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.network.Route
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess


/**
 * Represents a Guild (aka "server"), or a self-contained community of users. Guilds contain their own
 * [text][GuildTextChannel] and [voice][GuildVoiceChannel] channels, and can be customized further with
 * [roles][GuildRole] to segment members into different subgroups.
 *
 * @constructor Create a [Guild] instance from an internal [GuildData] instance
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val context: BotClient = data.context
    override val id: Long get() = data.id

    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of [User.username], and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    val name: String get() = data.name
    /** When the bot's user joined this guild. */
    val joinedAt: DateTimeTz get() = data.joinedAt

    /** The member who owns this guild. */
    val owner: GuildMember get() = data.owner.toMember()
    /** All members of this guild. */
    val members: List<GuildMember> get() = data.members.map { it.value.toMember() }
    /** All the roles of this guild. */
    val roles: List<GuildRole> get() = data.roles.map { it.value.lazyEntity }

    /** A list of all channels in this guild. */
    val channels: List<GuildChannel> get() = data.allChannels.map { it.value.lazyEntity }
    /** A list of all text channels in this guild. */
    val textChannels: List<GuildTextChannel> get() = channels.filterIsInstance<GuildTextChannel>()
    /** A [List] of all voice channels in this guild. */
    val voiceChannels: List<GuildVoiceChannel> get() = channels.filterIsInstance<GuildVoiceChannel>()
    /** A [List] of all [channel categories][GuildChannelCategory] in this [Guild]. */
    val channelCategories: List<GuildChannelCategory> get() = channels.filterIsInstance<GuildChannelCategory>()
    /** The channel to which system messages are sent. */
    val systemChannel: GuildTextChannel? get() = data.systemChannel?.lazyEntity
    /** The channel for the server widget. */
    val widgetChannel: GuildChannel? get() = data.widgetChannel?.lazyEntity
    /** The [GuildVoiceChannel] to which AFK members are sent to after not speaking for [afkTimeout] seconds. */
    val afkChannel: GuildVoiceChannel? get() = data.afkChannel?.lazyEntity
    /** The AFK timeout in seconds. */
    val afkTimeout: Int get() = data.afkTimeout.toInt()

    /** [Permissions][Permission] for the client in the [Guild] (not including channel overrides). */
    val permissions: Set<Permission> get() = data.permissions

    /**
     * Whether [members][GuildMember] who have not explicitly set their notification settings will receive
     * a notification for every [message][Message] in this [Guild]. (`ALL` or Only `@Mentions`)
     */
    val defaultMessageNotifications: MessageNotificationLevel get() = data.defaultMessageNotifications
    /** How broadly, if at all, should Discord automatically filter [messages][Message] for explicit content. */
    val explicitContentFilter: ExplicitContentFilterLevel get() = data.explicitContentFilter
    /** Enabled guild features. */
    val enabledFeatures: List<String> get() = data.features
    /** The [VerificationLevel] required for the [Guild]. */
    val verificationLevel: VerificationLevel get() = data.verificationLevel
    /** The [Multi-Factor Authentication Level][MfaLevel] required to send [messages][Message] in this [Guild]. */
    val mfaLevel: MfaLevel get() = data.mfaLevel
    /** Is this [Guild] embeddable (e.g. widget). */
    val isEmbedEnabled: Boolean get() = data.isEmbedEnabled
    /** The [Channel] that the widget will generate an invite to. */
    val embedChannel: GuildChannel? get() = data.embedChannel?.lazyEntity

    /** The Guild Icon image hash. Used to form the URI to the image. */
    val icon: String? get() = data.iconHash
    /** The [Guild]'s splash image, which is shown in invites. */
    val splashImage: String? get() = data.splashHash
    /** The region/locale of the Guild. */
    val region: String get() = data.region
    /** `true` if this [Guild] is considered "large" by Discord. */
    val isLarge: Boolean? get() = data.isLarge


    /**
     * Kick a [GuildMember] from this [Guild]. This requires [Permission.KickMembers].
     * Returns `true` if the [GuildMember] was successful kicked from the [Guild]
     */
    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(Route.RemoveGuildMember(id, user.id)).status.isSuccess()

    /**
     * Ban a [GuildMember] from this [Guild] and delete their messages from all [text channels][TextChannel]
     * from the past [deleteMessageDays] days ``(0-7)``. This requires [Permission.BanMembers].
     * @return `true` if the [GuildMember] was successful banned from the [Guild]
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(Route.CreateGuildBan(id, user.id, deleteMessageDays, reason))
            .status.isSuccess()

    /** Leaves this [Guild] */
    suspend fun leave() {
        context.requester.sendRequest(Route.LeaveGuild(id))
    }

    companion object {
        /** The minimum character length for a [Guild.name] */
        const val NAME_MIN_LENGTH: Int = 2
        /** The maximum character length for a [Guild.name] */
        const val NAME_MAX_LENGTH: Int = 32
        /** The allowed range of character length for a [Guild.name] */
        val NAME_LENGTH_RANGE: IntRange = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

/**
 * A [GuildMember] is a [User] associated with a specific [Guild (aka server)][Guild]. A [GuildMember] holds
 * data about the encased [User] which exists only in the respective [Guild].
 *
 * @constructor Builds a [GuildMember] object from data within a [GuildMemberData].
 */
class GuildMember internal constructor(private val data: GuildMemberData) {
    /** The backing user of this member. */
    val user: User get() = data.user.lazyEntity
    /** The guild in which this member resides. */
    val guild: Guild get() = data.guild.lazyEntity
    /** The roles that this member belongs to. */
    val roles: List<GuildRole> get() = data.roles.map { it.lazyEntity }
    /** An optional [nickname] which is used as an alias for the member in their guild. */
    val nickname: String? get() = data.nickname
    /** The date and time when the [user] joined the [guild]. */
    val joinedAt: DateTimeTz get() = data.joinedAt
    /** Whether this member is deafened in [Voice Channels][GuildVoiceChannel]. */
    val isDeafened: Boolean get() = data.isDeafened
    /** Whether the [GuildMember] is muted in [Voice Channels][GuildVoiceChannel]. */
    val isMuted: Boolean get() = data.isMuted

    /** Checks if this guild member is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildMember && other.user == user && other.guild == guild
}

/**
 * Whether [members][GuildMember] who have not explicitly set their notification settings will receive
 * a notification for every [message][Message] in this [Guild].
 */
enum class MessageNotificationLevel {
    /** A notification will be sent on each message. */
    ALL_MESSAGES,
    /** A notification will be sent ONLY when the [GuildMember] is mentioned. */
    ONLY_MENTIONS
}

/**
 * How broadly, if at all, [messages][Message] will be filtered for explicit content.
 */
enum class ExplicitContentFilterLevel {
    /** Discord will not scan any messages. */
    DISABLED,
    /** Discord will scan messages from any [GuildMember] without a [GuildRole]. */
    MEMBERS_WITHOUT_ROLES,
    /** Discord will scan all messages sent, regardless of their author. */
    ALL_MEMBERS
}

/** Multi-factor Authentication level of a [Guild]. */
enum class MfaLevel {
    /** No multi-factor authentication requirement is in place. */
    NONE,
    /**
     * In order for a user to take administrative action, they must have multi-factor authentication on their Discord
     * account.
     */
    ELEVATED
}

/**
 * The verification criteria needed for users to send a [Message] either within a [Guild]
 * or directly to any [GuildMember] in a [Guild].
 */
enum class VerificationLevel {
    /** No verification required. */
    NONE,
    /** Must have a verified email. */
    LOW,
    /** [LOW] + must be registered on Discord for longer than 5 minutes. */
    MEDIUM,
    /** [MEDIUM] + must be a [GuildMember] of this [Guild] for longer than 10 minutes. */
    HIGH,
    /** [HIGH] + must have a verified phone on their Discord account. */
    VERY_HIGH
}
