package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.data.Presence
import com.serebit.strife.data.toBitSet
import com.serebit.strife.internal.encodeBase64
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.CreateGuildEmojiPacket
import com.serebit.strife.internal.packets.CreateGuildRolePacket
import com.serebit.strife.internal.packets.ModifyGuildEmojiPacket
import com.serebit.strife.internal.packets.ModifyGuildMemberPacket
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
    /** The Guild Icon image hash. Used to form the URI to the image. */
    val icon: String? get() = data.iconHash
    /** The [Guild]'s splash image, which is shown in invites. */
    val splashImage: String? get() = data.splashHash
    /** The region/locale of the Guild. */
    val region: String get() = data.region
    /** `true` if this [Guild] is considered "large" by Discord. */
    val isLarge: Boolean? get() = data.isLarge

    /** A list of all [channels][GuildChannel] in this [Guild]. */
    val channels: List<GuildChannel> get() = data.channelList.map { it.lazyEntity }
    /** A list of all [text channels][GuildTextChannel] in this [Guild]. */
    val textChannels: List<GuildTextChannel> get() = channels.filterIsInstance<GuildTextChannel>()
    /** A list of all [voice channels][GuildVoiceChannel] in this [Guild]. */
    val voiceChannels: List<GuildVoiceChannel> get() = channels.filterIsInstance<GuildVoiceChannel>()
    /** A list of all [channel categories][GuildChannelCategory] in this [Guild]. */
    val channelCategories: List<GuildChannelCategory> get() = channels.filterIsInstance<GuildChannelCategory>()

    /** All the [roles][GuildRole] of this [Guild]. */
    val roles: List<GuildRole> get() = data.roles.values.map { it.lazyEntity }
    /** All the [emojis][GuildEmoji] of this [Guild]. */
    val emojis: List<GuildEmoji> get() = data.emojiList.map { it.lazyEntity }
    /** All [members][GuildMember] of this [Guild]. */
    val members: List<GuildMember> get() = data.memberList.map { it.lazyMember }

    /** A list of all [presences][Presence] of members of this [Guild]. */
    val presences: List<Presence> get() = data.presenceList.toList()

    /** The channel to which system messages are sent. */
    val systemChannel: GuildTextChannel? get() = data.systemChannel?.lazyEntity
    /** The channel for the server widget. */
    val widgetChannel: GuildChannel? get() = data.widgetChannel?.lazyEntity

    /** The [GuildVoiceChannel] to which AFK members are sent to after not speaking for [afkTimeout] seconds. */
    val afkChannel: GuildVoiceChannel? get() = data.afkChannel?.lazyEntity
    /** The AFK timeout in seconds. */
    val afkTimeout: Int get() = data.afkTimeout.toInt()

    /** Is this [Guild] embeddable (e.g. widget). */
    val isEmbedEnabled: Boolean get() = data.isEmbedEnabled
    /** The [Channel] that the widget will generate an invite to. */
    val embedChannel: GuildChannel? get() = data.embedChannel?.lazyEntity

    /**
     * Whether [members][GuildMember] who have not explicitly set their notification settings will receive a
     * notification for every [message][Message] in this [Guild]. (`ALL` or `Only @Mentions`)
     */
    val defaultMessageNotifications: MessageNotificationLevel get() = data.defaultMessageNotifications
    /** How broadly, if at all, should Discord automatically filter [messages][Message] for explicit content. */
    val explicitContentFilter: ExplicitContentFilterLevel get() = data.explicitContentFilter
    /** The [VerificationLevel] required for the [Guild]. */
    val verificationLevel: VerificationLevel get() = data.verificationLevel
    /** The [Multi-Factor Authentication Level][MfaLevel] required to send [messages][Message] in this [Guild]. */
    val mfaLevel: MfaLevel get() = data.mfaLevel
    /** A list of enabled features in this [Guild]. */
    val enabledFeatures: List<String> get() = data.features

    /** When the bot's user joined this [Guild]. */
    val joinedAt: DateTimeTz? get() = data.joinedAt
    /** [Permissions][Permission] for the client in the [Guild] (not including channel overrides). */
    val permissions: Set<Permission> get() = data.permissions

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

    /** Leave this [Guild]. */
    suspend fun leave() {
        context.requester.sendRequest(Route.LeaveGuild(id))
    }

    /** Get a [channel][GuildChannel] by its [id][channelID]. Returns `null` if no such channel exist. */
    fun getChannel(channelID: Long): GuildChannel? = data.getChannelData(channelID)?.lazyEntity

    /** Get a [text channel][GuildTextChannel] by its [id][channelID]. Returns `null` if no such channel exist. */
    fun getTextChannel(channelID: Long): GuildTextChannel? = getChannel(channelID) as? GuildTextChannel

    /** Get a [voice channel][GuildVoiceChannel] by its [id][channelID]. Returns `null` if no such channel exist. */
    fun getVoiceChannel(channelID: Long): GuildVoiceChannel? = getChannel(channelID) as? GuildVoiceChannel

    /** Get a [role][GuildRole] by its [id][roleID]. Returns `null` if no such role exist. */
    fun getRole(roleID: Long): GuildRole? = data.getRoleData(roleID)?.lazyEntity

    /**
     * Create a new [GuildRole]. Set its [name], [permissions], [color], whether it is [mentionable] and whether to
     * [display it separately in the sidebar][hoist]. Returns the created [GuildRole] if successful, otherwise `null`.
     * *Requires [Permission.ManageRoles]*
     */
    suspend fun createRole(
        name: String? = null,
        permissions: List<Permission> = emptyList(),
        color: Color,
        hoist: Boolean = false,
        mentionable: Boolean = false
    ) = context.requester.sendRequest(
        Route.CreateGuildRole(id, CreateGuildRolePacket(name, permissions.toBitSet(), color.rgb, hoist, mentionable))
    ).status.isSuccess()


    /** Get an [emoji][GuildEmoji] by its [id][emojiID]. Returns `null` if no such emoji exist. */
    fun getEmoji(emojiID: Long): GuildEmoji? = data.getEmojiData(emojiID)?.lazyEntity

    /**
     * Create a new [GuildEmoji] in this [Guild] using the provided [name] and [imageData]. **Requires
     * [Permission.ManageEmojis].** The size of the emoji file must be less than 256kb. Additionally, you can whitelist
     * some [roles] to use this emoji.
     *
     * Returns the new [GuildEmoji], or `null` if the request has failed.
     */
    suspend fun createEmoji(name: String, imageData: ByteArray, roles: List<GuildRole> = listOf()): GuildEmoji? {
        require(imageData.size <= 256_000) { "Image file size must be less than 256kb (was ${imageData.size})." }

        return context.requester.sendRequest(
            Route.CreateGuildEmoji(id, CreateGuildEmojiPacket(name, encodeBase64(imageData), roles.map { it.id }))
        ).value?.toData(data, context)?.lazyEntity
    }

    /**
     * Modify the provided [emoji]'s [name] and [roles]. **Requires [Permission.ManageEmojis].**
     *
     * Returns the updated [GuildEmoji], or `null` on failure.
     */
    suspend fun modifyEmoji(emoji: GuildEmoji, name: String, roles: List<GuildRole>): GuildEmoji? = context.requester
        .sendRequest(Route.ModifyGuildEmoji(id, emoji.id, ModifyGuildEmojiPacket(name, roles.map { it.id })))
        .value
        ?.toData(data, context)
        ?.lazyEntity

    /**
     * Delete the provided [emoji] from this [Guild]. **Requires [Permission.ManageEmojis].**
     *
     * Returns `true` on success.
     */
    suspend fun deleteEmoji(emoji: GuildEmoji): Boolean = context.requester.sendRequest(
        Route.DeleteGuildEmoji(id, emoji.id)
    ).status.isSuccess()

    /**
     * Get a [GuildMember] in this [Guild] by their [id][memberID]. Returns a [GuildMember], or `null` if no such
     * member was found with this [id][memberID].
     */
    suspend fun getMember(memberID: Long): GuildMember? = data.getMemberData(memberID)?.lazyMember
        ?: context.requester.sendRequest(Route.GetGuildMember(id, memberID))
            .value
            ?.let { data.update(it) }
            ?.lazyMember


    /** Get the owner of this guild as [GuildMember]. */
    suspend fun getOwner(): GuildMember = getMember(data.ownerID)!!

    /** Get the [Presence] of a [member][GuildMember] by their [id][memberID]. Returns `null` if no presence found. */
    fun getPresence(memberID: Long): Presence? = data.getPresence(memberID)

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
    /** The [Presence] of this [member][GuildMember] in the [guild]. */
    val presence: Presence? get() = data.guild.getPresence(data.user.id)

    /**
     * Set this [GuildMember]'s [nickname][GuildMember.nickname]. Returns `true` if the nickname was changed.
     * *Requires [Permission.ManageNicknames] or [Permission.ChangeNickname] (if self)*.
     */
    suspend fun setNickname(nickname: String): Boolean {
        val route = if (user.id != guild.context.selfUserID)
            Route.ModifyGuildMember(guild.id, user.id, ModifyGuildMemberPacket(nick = nickname))
        else Route.ModifyCurrentUserNick(guild.id, nickname)
        return guild.context.requester.sendRequest(route).status.isSuccess()
    }

    /** Give this [GuildMember] the [role]. Returns `true` if successful. *Requires [Permission.ManageRoles].* */
    suspend fun addRole(role: GuildRole): Boolean = addRole(role.id)

    /**
     * Give this [GuildMember] the [GuildRole] with this [roleID].
     * Returns `true` if successful. *Requires [Permission.ManageRoles].*
     */
    suspend fun addRole(roleID: Long): Boolean = guild.context.requester.sendRequest(
        Route.AddGuildMemberRole(guild.id, user.id, roleID)
    ).status.isSuccess()

    /** Remove the [role] from this [GuildMember]. Returns `true` if successful. *Requires [Permission.ManageRoles].* */
    suspend fun removeRole(role: GuildRole): Boolean = removeRole(role.id)

    /**
     * Remove the [GuildRole] with this [roleID] from this [GuildMember]. Returns `true` if successful.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun removeRole(roleID: Long): Boolean = guild.context.requester.sendRequest(
        Route.RemoveGuildMemberRole(guild.id, user.id, roleID)
    ).status.isSuccess()

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
