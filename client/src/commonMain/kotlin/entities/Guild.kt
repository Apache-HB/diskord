package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.*
import com.serebit.strife.internal.encodeBase64
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.entitydata.GuildMessageChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.BanPacket
import com.serebit.strife.internal.packets.toIntegration
import com.serebit.strife.internal.packets.toInvite
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
     * Returns `true` if the [GuildMember] was successful banned from the [Guild]
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(Route.CreateGuildBan(id, user.id, deleteMessageDays, reason))
            .status.isSuccess()

    /** Unban a [User] from this [Guild]. Returns `true` if the user was successfully unbaned. */
    suspend fun unban(userID: Long): Boolean =
        context.requester.sendRequest(Route.RemoveGuildBan(id, userID)).status.isSuccess()

    /**
     * Returns the list of [GuildBan]s for this [Guild] which optionally match the given filters
     * for [reason] and [userID] or `null` if the request failed.
     */
    suspend fun getBans(userID: Long? = null, reason: String? = null): List<GuildBan>? = context.requester.sendRequest(
        Route.GetGuildBans(id)
    ).value?.map { it.toGuildBan(context) }
        ?.filter { b -> userID?.let { b.userID == it } ?: true && reason?.let { b.reason == it } ?: true }

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
     * [display it separately in the sidebar][hoist]. Returns the ID of the created role if successful, otherwise `null`.
     * *Requires [Permission.ManageRoles]*
     */
    suspend fun createRole(
        name: String? = null,
        permissions: List<Permission> = emptyList(),
        color: Color,
        hoist: Boolean = false,
        mentionable: Boolean = false
    ): Long? = context.requester.sendRequest(
        Route.CreateGuildRole(id, name, permissions.toBitSet(), color.rgb, hoist, mentionable)
    ).value?.id

    /**
     * Set the Role with id [roleID]'s [position][GuildRole.position].
     * Returns `true` on success. *Requires [Permission.ManageRoles].*
     */
    suspend fun setRolePosition(roleID: Long, position: Int): Boolean {
        val hr = getSelfMember()?.highestRole
        require((hr?.position?.compareTo(position) ?: 1) > 0) {
            "New GuildRole Position cannot outrank the current client."
        }
        return roles.filterNot { r -> hr?.let { r > it } != false || r.id == roleID }
            .sortedBy { it.position }
            .map { it.id }
            .toMutableList()
            .apply { add(position, roleID) }
            .let { setRolePositions(it) }
    }

    /**
     * Set the [positions][GuildRole.position] of this guild's [roles] using an [orderedCollection].
     * Any [GuildRole] not included will be appended to the given [orderedCollection].
     * The [last][Collection.last] role will be at the top of the hierarchy.
     * Returns `true` on success. *Requires [Permission.ManageRoles].*
     */
    suspend fun setRolePositions(orderedCollection: Collection<Long>): Boolean {
        require(orderedCollection.isNotEmpty()) { "Role positions cannot be empty." }
        val hr = getSelfMember()?.highestRole
        val oRp = roles
            .filterNot { r -> hr?.let { r > it } != false || r.id in orderedCollection }
            .sortedBy { it.position }
            .map { it.id }
        val rp = (orderedCollection + oRp).mapIndexed { index, id -> Pair(id, index + 1) }
        return context.requester.sendRequest(Route.ModifyGuildRolePosition(id, rp.toMap())).status.isSuccess()
    }

    /**
     * Delete [GuildRole] with the given [roleID]. Use this method if only the role ID is available, otherwise the
     * recommended method to use is [GuildRole.delete] (though they are functionally the same).
     */
    suspend fun deleteRole(roleID: Long): Boolean =
        context.requester.sendRequest(Route.DeleteGuildRole(id, roleID)).status.isSuccess()

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
            Route.CreateGuildEmoji(id, name, encodeBase64(imageData), roles.map { it.id })
        ).value?.toData(data, context)?.lazyEntity
    }

    /**
     * Modify the provided [emoji]'s [name] and [roles]. **Requires [Permission.ManageEmojis].**
     *
     * Returns the updated [GuildEmoji], or `null` on failure.
     */
    suspend fun modifyEmoji(emoji: GuildEmoji, name: String, roles: List<GuildRole>): GuildEmoji? = context.requester
        .sendRequest(Route.ModifyGuildEmoji(id, emoji.id, name, roles.map { it.id }))
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

    /**
     * Get the [Invite]s to this [Guild]. Returns the [Invite]s maped to their [code][Invite.code]
     * or `null` if the request failed.
     */
    suspend fun getInvites(): Map<String, Invite>? = context.requester.sendRequest(Route.GetGuildInvites(id)).value
        ?.map { ip -> ip.toInvite(context, this, members.firstOrNull { it.user.id == ip.inviter.id }) }
        ?.associateBy { it.code }

    /** Delete's the [Invite] with the given [code]. Returns `true` if successful. */
    suspend fun deleteInvite(code: String): Boolean =
        context.requester.sendRequest(Route.DeleteInvite(code)).status.isSuccess()

    /**
     * Returns the number of [GuildMember]s that would be removed by a [prune] of [days] number of days,
     * or `null` if the request failed.
     *
     * *Defaults to 7 days. Requires [Permission.KickMembers].*
     */
    suspend fun getPruneCount(days: Int = 7): Int? =
        context.requester.sendRequest(Route.GetGuildPruneCount(id, days)).value?.pruned

    /**
     * Beings a [prune operation](https://discordapp.com/developers/docs/resources/guild#begin-guild-prune) which
     * removes any [GuildMember] that joined between now and [days] days ago.
     *
     * If [withPruneCount] is set to `true`, this returns the number of [GuildMember]s that would be removed by a
     * [prune] of [days] number of days, or `null` if the request failed or [withPruneCount] is set to `false`.
     *
     * Note: Discord recommends setting [withPruneCount] to false for large [Guild]s.
     *
     * *Defaults [days]=7 and [withPruneCount]=false. Requires [Permission.KickMembers].*
     */
    suspend fun prune(days: Int = 7, withPruneCount: Boolean = true): Int? = context.requester.sendRequest(
        Route.BeginGuildPrune(id, days, withPruneCount)
    ).value?.pruned

    /** Returns all the [GuildIntegration]s of this [Guild] or `null` if the request failed. */
    suspend fun getAllIntegrations(): List<GuildIntegration>? =
        context.requester.sendRequest(Route.GetGuildIntegrations(id)).value
            ?.map { it.toIntegration(context, this, getMember(it.user.id)!!) }

    /**
     * Creates a new [GuildIntegration] with the bot client as the [member][GuildIntegration.member].
     * Returns `true` if the new integration was created
     */
    suspend fun createIntegration(id: Long, type: String): Boolean =
        context.requester.sendRequest(Route.CreateGuildIntegration(id, type, id)).status.isSuccess()

    /**
     *  Deletes the [GuildIntegration] with the given [integrationID]. Returns `true` if deleted.
     * *Requires [Permission.ManageGuild]*
     */
    suspend fun deleteIntegration(integrationID: Long): Boolean =
        context.requester.sendRequest(Route.DeleteGuildIntegration(id, integrationID)).status.isSuccess()

    /** Returns the [Guild]'s [AuditLog] or `null` if the request failed. */
    suspend fun getAuditLog(): AuditLog? = context.requester.sendRequest(Route.GetGuildAuditLog(id, limit = 100))
        .value?.toAuditLog(data)

    /** Returns the [GuildEmbed] for this [Guild] or `null` if the request failed. */
    suspend fun getGuildEmbed(): GuildEmbed? = context.requester.sendRequest(Route.GetGuildEmbed(id)).value
        ?.run { GuildEmbed(this@Guild, enabled, channel_id?.let { getChannel(it) }) }

    /** Returns the vanity URL or `null` if not set or the request failed. *Requires [Permission.ManageGuild].* */
    suspend fun getVanityUrl(): String? = context.requester.sendRequest(Route.GetGuildVanityUrl(id)).value?.code

    /** Get all [webhooks][Webhook] of this [Guild]. Returns a [List] of [Webhook], or `null` on failure. */
    suspend fun getWebhooks(): List<Webhook>? = context.requester.sendRequest(Route.GetChannelWebhooks(id))
        .value
        ?.map { it.toEntity(context, data, data.getChannelData(it.channel_id) as GuildMessageChannelData<*, *>) }

    companion object {
        /** The minimum character length for a [Guild.name] */
        const val NAME_MIN_LENGTH: Int = 2
        /** The maximum character length for a [Guild.name] */
        const val NAME_MAX_LENGTH: Int = 32
        /** The allowed range of character length for a [Guild.name] */
        val NAME_LENGTH_RANGE: IntRange = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

/** Returns the current [BotClient] member of this [Guild] or `null` if the request failed. */
suspend fun Guild.getSelfMember(): GuildMember? = getMember(context.selfUserID)

/** The @everyone [GuildRole] applied to all [GuildMember]s for base [Permission] settings. */
val Guild.`@everyone`: GuildRole get() = getRole(id)!!

/**
 * Set the [positions][GuildRole.position] of this guild's [roles][Guild.roles] using an [orderedCollection].
 * Any [GuildRole] not included will be appended to the given [orderedCollection].
 * The [last][Collection.last] role will be at the top of the hierarchy.
 * Returns `true` on success. *Requires [Permission.ManageRoles].*
 */
suspend fun Guild.setRolePositions(orderedCollection: Collection<GuildRole>): Boolean =
    setRolePositions(orderedCollection.map { it.id })

/**
 * Set the [positions][GuildRole.position] of these [GuildRole]s in their [Guild].
 * The [last][Collection.last] role will be at the top of the hierarchy.
 * Returns `true` on success. *Requires [Permission.ManageRoles].*
 */
suspend fun Collection<GuildRole>.setPositions(): Boolean {
    require(isNotEmpty()) { "Collections must contain at least one GuildRole to set positions." }
    require(all { it.guildId == first().guildId }) { "All GuildRoles must be from the same Guild." }
    return first().getGuild().setRolePositions(this)
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
    /** The highest ranking role this member has. */
    val highestRole: GuildRole? get() = roles.maxBy { it.position }
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
    /** The [VoiceState] of this [member][GuildMember] in the [guild]. */
    val voiceState: VoiceState? get() = data.guild.getVoiceState(data.user.id)

    /**
     * Set this [GuildMember]'s [nickname][GuildMember.nickname]. Returns `true` if the nickname was changed.
     * *Requires [Permission.ManageNicknames] or [Permission.ChangeNickname] (if self)*.
     */
    suspend fun setNickname(nickname: String): Boolean {
        val route = if (user.id == guild.context.selfUserID) Route.ModifyCurrentUserNick(guild.id, nickname)
        else Route.ModifyGuildMember(guild.id, user.id, nick = nickname)
        return guild.context.requester.sendRequest(route).status.isSuccess()
    }

    /**
     * Give this [GuildMember] the [GuildRole] with this [roleID].
     * Returns `true` if successful. *Requires [Permission.ManageRoles].*
     */
    suspend fun addRole(roleID: Long): Boolean =
        guild.context.requester.sendRequest(Route.AddGuildMemberRole(guild.id, user.id, roleID)).status.isSuccess()

    /**
     * Remove the [GuildRole] with this [roleID] from this [GuildMember]. Returns `true` if successful.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun removeRole(roleID: Long): Boolean =
        guild.context.requester.sendRequest(Route.RemoveGuildMemberRole(guild.id, user.id, roleID)).status.isSuccess()

    /**
     * Set whether the [GuildMember] is deafened in [Voice Channels][GuildVoiceChannel].
     * Returns `true` if successful.
     */
    suspend fun setDeafen(deafened: Boolean): Boolean {
        require(this.voiceState?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to set deafen state."
        }
        return guild.context.requester.sendRequest(Route.ModifyGuildMember(guild.id, user.id, deaf = deafened))
            .status.isSuccess()
    }

    /**
     * Set whether the [GuildMember] is muted in [Voice Channels][GuildVoiceChannel].
     * Returns `true` if successful.
     */
    suspend fun setMuted(muted: Boolean): Boolean {
        require(this.voiceState?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to set mute state."
        }
        return guild.context.requester.sendRequest(
            Route.ModifyGuildMember(guild.id, user.id, mute = muted)
        ).status.isSuccess()
    }

    /** Move the [GuildMember] to another [GuildVoiceChannel]. Requires the member is already in a voice channel. */
    suspend fun move(channelID: Long): Boolean {
        require(this.voiceState?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to move channels."
        }
        return guild.context.requester.sendRequest(Route.ModifyGuildMember(guild.id, user.id, channel_id = channelID))
            .status.isSuccess()
    }

    /** Checks if this guild member is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildMember && other.user == user && other.guild == guild
}

/** Give this [GuildMember] the [role]. Returns `true` if successful. *Requires [Permission.ManageRoles].* */
suspend fun GuildMember.addRole(role: GuildRole): Boolean = addRole(role.id)

/** Remove the [role] from this [GuildMember]. Returns `true` if successful. *Requires [Permission.ManageRoles].* */
suspend fun GuildMember.removeRole(role: GuildRole): Boolean = removeRole(role.id)

/** Deafen the [GuildMember] in [Voice Channels][GuildVoiceChannel]. Returns `true` if the member was deafened. */
suspend fun GuildMember.deafen(): Boolean = setDeafen(true)

/** UnDeafen the [GuildMember] in [Voice Channels][GuildVoiceChannel]. Returns `true` if the member was undeafened. */
suspend fun GuildMember.unDeafen(): Boolean = setDeafen(false)

/** Mute the [GuildMember] in [Voice Channels][GuildVoiceChannel]. Returns `true` if the member was muted. */
suspend fun GuildMember.mute(): Boolean = setMuted(true)

/** Unmute the [GuildMember] in [Voice Channels][GuildVoiceChannel]. Returns `true` if the member was unmuted. */
suspend fun GuildMember.unMute(): Boolean = setMuted(false)

/** Move the [GuildMember] to another [GuildVoiceChannel]. Requires the member is already in a voice channel. */
suspend fun GuildMember.move(voiceChannel: GuildVoiceChannel): Boolean = move(voiceChannel.id)

/**
 * A [GuildBan] represents aa banning of a [User] from a [Guild].
 *
 * @property reason The reason for this ban.
 * @property userID The ID of the banned [User]./
 * @property user The banned [User].
 */
data class GuildBan(val reason: String?, val userID: Long, val user: User)

internal fun BanPacket.toGuildBan(context: BotClient) = GuildBan(reason, user.id, user.toData(context).lazyEntity)

/**
 * A [GuildIntegration] is a connection between a third-party API and a [Guild]. For examples and more information
 * [see](https://discordapp.com/streamkit)
 *
 * @property guild The [Guild] which this integration is in
 * @property name The name of the Integration
 * @property type YouTube, Twitch, etc
 * @property enabled Whether this integration is enabled
 * @property syncing Whether this integration is being synchronized
 * @property role The [GuildRole] this integration uses for subscribers
 * @property expireBehavior The behavior for expiring subscribers
 * @property gracePeriod The grace period (in days) before a subscriber is expired
 * @property member The [GuildMember] which "owns" this integration
 * @property account The integration's account information
 * @property lastSync When this integration was last synchronized
 * @property emojiEnabled whether emoticons should be synced for this integration (twitch only as of Strife 0.3.0)
 */
class GuildIntegration internal constructor(
    override val context: BotClient,
    override val id: Long,
    val guild: Guild,
    val name: String,
    val type: String,
    val enabled: Boolean,
    val syncing: Boolean,
    val role: GuildRole,
    expireBehavior: ExpireBehavior,
    gracePeriod: Int,
    val member: GuildMember,
    val account: Account,
    val lastSync: DateTimeTz
) : Entity {

    var emojiEnabled: Boolean = type == "twitch"
        private set
    var gracePeriod: Int = gracePeriod
        private set
    var expireBehavior: ExpireBehavior = expireBehavior
        private set

    /**
     * The [Account] of an [GuildIntegration].
     *
     * @property id The unique ID of this account
     * @property name the Name of this Account
     */
    data class Account(val id: String, val name: String)

    /** The behavior of expiring subscribers. */
    enum class ExpireBehavior {
        /** Remove the [role] from the [GuildMember] when their subscription expires. */
        REMOVE_ROLE,
        /** Kick the [GuildMember] when their subscription expires. */
        KICK
    }

    /** Set the [expireBehavior]. Returns `true` if set successfully. */
    suspend fun setExpireBehavior(behavior: ExpireBehavior): Boolean = context.requester.sendRequest(
        Route.ModifyGuildIntegration(guild.id, id, behavior.ordinal, gracePeriod, emojiEnabled)
    ).status.isSuccess()
        .also { if (it) this.expireBehavior = behavior }

    /** Set the [gracePeriod]. Returns `true` if set successfully. Must be 1, 3, 7, 14, or 30 days. */
    suspend fun setGracePeriod(days: Int): Boolean {
        require(days in listOf(1, 3, 7, 14, 30)) { "Grace Period must be 1, 3, 7, 14, or 30 days." }
        return context.requester.sendRequest(
            Route.ModifyGuildIntegration(guild.id, id, expireBehavior.ordinal, days, emojiEnabled)
        ).status.isSuccess()
            .also { if (it) this.gracePeriod = days }
    }

    /** Set [emojiEnabled]. Returns `true` if set successfully. */
    suspend fun setEmojiEnabled(enabled: Boolean): Boolean = context.requester.sendRequest(
        Route.ModifyGuildIntegration(guild.id, id, expireBehavior.ordinal, gracePeriod, enabled)
    ).status.isSuccess()
        .also { if (it) this.emojiEnabled = enabled }

    /** Synchronize the integration. Returns `true` if successful. *Requires [Permission.ManageGuild].* */
    suspend fun sync(): Boolean =
        context.requester.sendRequest(Route.SyncGuildIntegration(guild.id, id)).status.isSuccess()

    /** Deletes this [GuildIntegration]. Returns `true` if deleted. *Requires [Permission.ManageGuild].* */
    suspend fun delete(): Boolean = guild.deleteIntegration(id)
}

/**
 * The [GuildEmbed] is used when embedding a [Guild] in a web-page.
 *
 * @property guild The [Guild] this exists in.
 */
class GuildEmbed(val guild: Guild, enabled: Boolean, channel: GuildChannel?) {

    /** The channel of the [GuildEmbed]. */
    var channel: GuildChannel? = channel
        private set
    /** Whether the [GuildEmbed] is enabled. */
    var enabled: Boolean = enabled
        private set

    /** Set the [channel]. Returns `true` if successful. */
    suspend fun setChannel(channelID: Long): Boolean = guild.context.requester.sendRequest(
        Route.ModifyGuildEmbed(guild.id, channelID = channelID)
    ).status.isSuccess().also { if (it) this.channel = guild.getChannel(channelID) }

    /** Set the [channel]. Returns `true` if successful. */
    suspend fun setChannel(guildChannel: GuildChannel): Boolean = setChannel(guildChannel.id)

    /** Set [enabled]. Returns `true` if successful. */
    suspend fun setEnabled(enabled: Boolean): Boolean =
        guild.context.requester.sendRequest(Route.ModifyGuildEmbed(guild.id, enabled)).status.isSuccess()
            .also { if (it) this.enabled = enabled }

    /** Enable the [GuildEmbed]. Returns `true` if successful. */
    suspend fun enable(): Boolean = setEnabled(true)

    /** Disable the [GuildEmbed]. Returns `true` if successful. */
    suspend fun disable(): Boolean = setEnabled(false)
}

/**
 * Represents a code that when used, adds a [User] to a [Guild] at a [GuildChannel].
 *
 * @property guild The Guild this invite is for.
 * @property code The unique invite code/ID.
 * @property channel The Guild Channel this invite is for.
 * @property approxPresences Approximate count of online [GuildMember]s.
 * @property approxMemberCount Approximate count of total [GuildMember]s.
 * @property inviter The User who created the invite.
 * @property targetUser The target user of this [Invite].
 * @property useCount Number of times this invite has been used.
 * @property useLimit Max number of times this invite can be used.
 * @property activeTimeRange The time during which this invite is active.
 * @property temporary Whether this invite only grants temporary membership.
 * @property revoked Whether this invite is revoked.
 */
data class Invite(
    val code: String,
    val useCount: Int,
    val useLimit: Int,
    val guild: Guild,
    val channel: GuildChannel,
    val inviter: GuildMember?,
    val targetUser: User? = null,
    val activeTimeRange: ClosedRange<DateTimeTz>,
    val approxPresences: Int? = null,
    val approxMemberCount: Int? = null,
    val temporary: Boolean,
    val revoked: Boolean
) {
    /** The link which opens this invite in a web browser. */
    val uri: String = "https://discord.gg/$code"

    /** Delete this [Invite]. Returns `true` is successful. */
    suspend fun delete(): Boolean = guild.deleteInvite(code)
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
