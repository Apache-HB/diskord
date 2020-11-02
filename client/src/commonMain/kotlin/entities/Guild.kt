package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.*
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMemberData
import com.serebit.strife.internal.entitydata.GuildMessageChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.*
import io.ktor.http.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant

/**
 * Represents a Guild (aka "server"), or a self-contained community of users. Guilds contain their own
 * [text][GuildTextChannel] and [voice][GuildVoiceChannel] channels, and can be customized further with
 * [roles][GuildRole] to segment members into different subgroups.
 */
class Guild internal constructor(private val data: GuildData) : Entity {
    override val context: BotClient = data.context
    override val id: Long get() = data.id

    /**
     * The name of a Guild is not unique across Discord, and as such, any two guilds can have the same name. Guild
     * names are subject to similar restrictions as those of usernames, and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 100 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     */
    suspend fun getName(): String = data.name

    /** The Guild Icon image hash. Used to form the URI to the image. */
    suspend fun getIcon(): String? = data.iconHash

    /** The [Guild]'s splash image, which is shown in invites. */
    suspend fun getSplashImage(): String? = data.splashHash

    /** The region/locale of the Guild. */
    suspend fun getRegion(): String = data.region

    /** `true` if this guild is considered "large" by Discord. */
    suspend fun isLarge(): Boolean? = data.isLarge

    /** A list of all [channels][GuildChannel] in this guild. */
    suspend fun getChannels(): List<GuildChannel> = data.channelList.map { it.lazyEntity }

    /** A list of all [text channels][GuildTextChannel] in this guild. */
    suspend fun getTextChannels(): List<GuildTextChannel> = getChannels().filterIsInstance<GuildTextChannel>()

    /** A list of all [voice channels][GuildVoiceChannel] in this guild. */
    suspend fun getVoiceChannels(): List<GuildVoiceChannel> = getChannels().filterIsInstance<GuildVoiceChannel>()

    /** A list of all [channel categories][GuildChannelCategory] in this guild. */
    suspend fun getChannelCategories(): List<GuildChannelCategory> =
        getChannels().filterIsInstance<GuildChannelCategory>()

    /** All the roles that this guild contains. */
    suspend fun getRoles(): List<GuildRole> = data.roles.values.map { it.lazyEntity }

    /** All the emojis that this guild contains. */
    suspend fun getEmojis(): List<GuildEmoji> = data.emojiList.map { it.lazyEntity }

    /** The members of this guild. On extremely large guilds, this may not return the entire member list. */
    suspend fun getMembers(): List<GuildMember> = data.memberList.map { it.lazyMember }

    /** A list of all [presences][Presence] of members of this guild. */
    suspend fun getPresences(): List<Presence> = data.presenceList.toList()

    /** The channel to which system messages are sent (i.e. user join messages). */
    suspend fun getSystemChannel(): GuildTextChannel? = data.systemChannel?.lazyEntity

    /** The channel for the server widget. */
    suspend fun getWidgetChannel(): GuildChannel? = data.widgetChannel?.lazyEntity

    /**
     * The voice to which AFK members are sent to after not speaking for the number of seconds given by [getAfkTimeout].
     */
    suspend fun getAfkChannel(): GuildVoiceChannel? = data.afkChannel?.lazyEntity

    /** The AFK timeout in seconds. */
    suspend fun getAfkTimeout(): Int = data.afkTimeout.toInt()

    /** Is this guild embeddable (e.g. widget). */
    suspend fun isEmbedEnabled(): Boolean = data.isEmbedEnabled

    /** The [Channel] that the widget will generate an invite to. */
    suspend fun getEmbedChannel(): GuildChannel? = data.embedChannel?.lazyEntity

    /**
     * Whether members who have not explicitly set their notification settings will receive a notification for every
     * message sent in this guild's text channels. (`ALL` or `Only @Mentions`)
     */
    suspend fun getDefaultMessageNotifications(): MessageNotificationLevel = data.defaultMessageNotifications

    /** How broadly, if at all, should Discord automatically filter messages for explicit content. */
    suspend fun getExplicitContentFilter(): ExplicitContentFilterLevel = data.explicitContentFilter

    /** The verification level required to join this guild. */
    suspend fun getVerificationLevel(): VerificationLevel = data.verificationLevel

    /** The Multi-Factor Authentication level required to send messages in this guild. */
    suspend fun getMfaLevel(): MfaLevel = data.mfaLevel

    /** A list of enabled features in this guild. */
    suspend fun getEnabledFeatures(): List<String> = data.features

    /** When the bot's user joined this guild. */
    suspend fun getJoinedAt(): Instant? = data.joinedAt

    /** The permissions granted  for the client in the [Guild] (not including channel overrides). */
    suspend fun getPermissions(): Set<Permission> = data.permissions

    /**
     * Kick a member from this guild. Requires [Permission.KickMembers].
     * Returns `true` if the member was successful kicked.
     */
    suspend fun kick(user: User): Boolean =
        context.requester.sendRequest(Route.RemoveGuildMember(id, user.id)).status.isSuccess()

    /**
     * Ban a member from this guild and delete their messages from all text channels from the past [deleteMessageDays]
     * days ``(0-7)``. This requires [Permission.BanMembers].
     * Returns `true` if the member was successfully banned.
     */
    suspend fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        context.requester.sendRequest(Route.CreateGuildBan(id, user.id, deleteMessageDays, reason))
            .status.isSuccess()

    /**
     * Unban a user from this guild by providing their [ID][userID]. Returns `true` if the user was successfully
     * unbanned.
     */
    suspend fun unban(userID: Long): Boolean =
        context.requester.sendRequest(Route.RemoveGuildBan(id, userID)).status.isSuccess()

    /**
     * Returns the list of [GuildBan]s for this guild which optionally match the given filters
     * for [reason] and [userID], or `null` if the request failed.
     */
    suspend fun getBans(userID: Long? = null, reason: String? = null): List<GuildBan>? = context.requester.sendRequest(
        Route.GetGuildBans(id)
    ).value?.map { it.toGuildBan(context) }
        ?.filter { b -> userID?.let { b.userID == it } ?: true && reason?.let { b.reason == it } ?: true }

    /** Leave this guild. */
    suspend fun leave() {
        context.requester.sendRequest(Route.LeaveGuild(id))
    }

    /** Get a channel in this guild by its [ID][channelID]. Returns `null` if no such channel exists. */
    fun getChannel(channelID: Long): GuildChannel? = data.getChannelData(channelID)?.lazyEntity

    /** Get a text channel by its [ID][channelID]. Returns `null` if no such channel exists. */
    fun getTextChannel(channelID: Long): GuildTextChannel? = getChannel(channelID) as? GuildTextChannel

    /** Get a voice channel by its [ID][channelID]. Returns `null` if no such channel exists. */
    fun getVoiceChannel(channelID: Long): GuildVoiceChannel? = getChannel(channelID) as? GuildVoiceChannel

    /** Get a role by its [ID][roleID]. Returns `null` if no such role exists. */
    fun getRole(roleID: Long): GuildRole? = data.getRoleData(roleID)?.lazyEntity

    /**
     * Create a new role. Set its [name], [permissions], [color], whether it is [mentionable] and whether to
     * [display it separately in the sidebar][hoist]. Returns the ID of the created role if successful, otherwise
     * `null`.
     *
     * **Requires [Permission.ManageRoles]**.
     */
    suspend fun createRole(
        name: String? = null,
        permissions: List<Permission> = emptyList(),
        color: Color,
        hoist: Boolean = false,
        mentionable: Boolean = false
    ): Long? = context.requester.sendRequest(
        Route.CreateGuildRole(id, CreateGuildRolePacket(name, permissions.toBitSet(), color.rgb, hoist, mentionable))
    ).value?.id

    /**
     * Set the role with the given [ID][roleID]'s [position] in the role hierarchy.
     * Returns `true` on success. **Requires [Permission.ManageRoles].**
     */
    suspend fun setRolePosition(roleID: Long, position: Int): Boolean {
        val hr = getSelfMember()?.getHighestRole()
        require((hr?.getPosition()?.compareTo(position) ?: 1) > 0) {
            "New GuildRole Position cannot outrank the current client."
        }
        return getRoles().filterNot { r -> hr?.let { r > it } != false || r.id == roleID }
            .asFlow()
            .map { it.getPosition() to it }
            .toList()
            .sortedBy { it.first }
            .map { it.second.id }
            .toMutableList()
            .apply { add(position, roleID) }
            .let { setRolePositions(it) }
    }

    /**
     * Set the positions of this guild's roles in the role hierarchy using an ordered collection.
     * Any role not included will be appended to the given [orderedCollection].
     * The last role will be at the top of the hierarchy.
     * Returns `true` on success. *Requires [Permission.ManageRoles].*
     */
    suspend fun setRolePositions(orderedCollection: Collection<Long>): Boolean {
        require(orderedCollection.isNotEmpty()) { "Role positions cannot be empty." }
        val hr = getSelfMember()?.getHighestRole()
        val oRp = getRoles()
            .filterNot { r -> hr?.let { r > it } != false || r.id in orderedCollection }
            .sortedBy { it.getPosition() }
            .map { it.id }
        val rp = (orderedCollection + oRp).mapIndexed { index, id -> Pair(id, index + 1) }
        return context.requester.sendRequest(Route.ModifyGuildRolePosition(id, rp.toMap())).status.isSuccess()
    }

    /**
     * Delete [GuildRole] with the given [ID][roleID]. Use this method if only the role ID is available, otherwise the
     * recommended method to use is [GuildRole.delete] (though they are functionally the same).
     */
    suspend fun deleteRole(roleID: Long): Boolean =
        context.requester.sendRequest(Route.DeleteGuildRole(id, roleID)).status.isSuccess()

    /** Get an emoji by its [ID][emojiID]. Returns `null` if no such emoji exists. */
    fun getEmoji(emojiID: Long): GuildEmoji? = data.getEmojiData(emojiID)?.lazyEntity

    /**
     * Create a new emoji in this guild using the provided [name] and [imageData]. **Requires
     * [Permission.ManageEmojis].** The size of the emoji file must be less than 256kb. Additionally, you can whitelist
     * some [roles] to use this emoji.
     *
     * Returns the new emoji, or `null` if the request has failed.
     */
    suspend fun createEmoji(name: String, imageData: ByteArray, roles: List<GuildRole> = emptyList()): GuildEmoji? {
        require(imageData.size <= 256_000) { "Image file size must be less than 256kb (was ${imageData.size})." }

        return context.requester.sendRequest(
            Route.CreateGuildEmoji(id, name, imageData, roles.map { it.id })
        ).value?.toData(data, context)?.lazyEntity
    }

    /**
     * Modify the provided [emoji]'s [name] and [roles]. **Requires [Permission.ManageEmojis].**
     *
     * Returns the updated emoji, or `null` on failure.
     */
    suspend fun modifyEmoji(emoji: GuildEmoji, name: String, roles: List<GuildRole>): GuildEmoji? = context.requester
        .sendRequest(Route.ModifyGuildEmoji(id, emoji.id, name, roles.map { it.id }))
        .value
        ?.toData(data, context)
        ?.lazyEntity

    /**
     * Delete the provided [emoji] from this guild. **Requires [Permission.ManageEmojis].**
     *
     * Returns `true` on success.
     */
    suspend fun deleteEmoji(emoji: GuildEmoji): Boolean = context.requester.sendRequest(
        Route.DeleteGuildEmoji(id, emoji.id)
    ).status.isSuccess()

    /** Gets a member of this guild by their [ID][memberID]. Returns `null` if no such member was found. */
    suspend fun getMember(memberID: Long): GuildMember? = data.getMemberData(memberID)?.lazyMember
        ?: context.requester.sendRequest(Route.GetGuildMember(id, memberID))
            .value
            ?.let { data.update(it) }
            ?.lazyMember

    /** Get the owner of this guild as a member. */
    suspend fun getOwner(): GuildMember = getMember(data.ownerID)!!

    /** Gets the [Presence] of a member by their [ID][memberID]. Returns `null` if no presence was found. */
    fun getPresence(memberID: Long): Presence? = data.getPresence(memberID)

    /**
     * Get the [Invite]s that belong to this guild. Returns the [Invite]s mapped to their [code][Invite.code]
     * or `null` if the request failed.
     */
    suspend fun getInvites(): Map<String, Invite>? = context.requester.sendRequest(Route.GetGuildInvites(id)).value
        ?.map { ip -> ip.toInvite(context, this, getMembers().firstOrNull { it.userID == ip.inviter.id }) }
        ?.associateBy { it.code }

    /** Delete's the [Invite] with the given [code]. Returns `true` if successful. */
    suspend fun deleteInvite(code: String): Boolean =
        context.requester.sendRequest(Route.DeleteInvite(code)).status.isSuccess()

    /**
     * Returns the number of members that would be removed by a [prune] of [days] number of days,
     * or `null` if the request failed.
     *
     * *Defaults to 7 days. Requires [Permission.KickMembers].*
     */
    suspend fun getPruneCount(days: Int = 7): Int? =
        context.requester.sendRequest(Route.GetGuildPruneCount(id, days)).value?.pruned

    /**
     * Starts a [prune operation](https://discordapp.com/developers/docs/resources/guild#begin-guild-prune) which
     * removes any member that joined between now and the given number of [days] ago.
     *
     * If [withPruneCount] is set to `true`, this returns the number of members that would be removed by such a
     * prune, or `null` if the request failed or [withPruneCount] is set to `false`.
     *
     * Note: Discord recommends setting [withPruneCount] to false for large guilds.
     *
     * *Defaults [days]=7 and [withPruneCount]=false. Requires [Permission.KickMembers].*
     */
    suspend fun prune(days: Int = 7, withPruneCount: Boolean = true): Int? = context.requester.sendRequest(
        Route.BeginGuildPrune(id, days, withPruneCount)
    ).value?.pruned

    /** Returns all the [GuildIntegration]s of this guild or `null` if the request failed. */
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

    /** Returns the [GuildEmbed] for this guild or `null` if the request failed. */
    suspend fun getGuildEmbed(): GuildEmbed? = context.requester.sendRequest(Route.GetGuildEmbed(id)).value
        ?.run { GuildEmbed(this@Guild, enabled, channel_id?.let { getChannel(it) }) }

    /** Returns the vanity URL or `null` if not set or the request failed. *Requires [Permission.ManageGuild].* */
    suspend fun getVanityUrl(): String? = context.requester.sendRequest(Route.GetGuildVanityUrl(id)).value?.code

    /** Get all [webhooks][Webhook] of this guild. Returns a [List] of [Webhook], or `null` on failure. */
    suspend fun getWebhooks(): List<Webhook>? = context.requester.sendRequest(Route.GetChannelWebhooks(id))
        .value
        ?.map { it.toEntity(context, data, data.getChannelData(it.channel_id) as GuildMessageChannelData<*, *>) }

    /** Returns a PNG image widget for the guild. Requires no permissions or authentication. */
    fun getGuildWidgetUri(style: GuildWidgetStyle? = null): String =
        "https://discord.com/api/guilds/$id/widget.png?style=${style?.urlParam.orEmpty()}"

    private suspend fun <T, R : Comparable<R>> Iterable<T>.sortedBy(selector: suspend (T) -> R?): List<T> =
        asFlow()
            .map { selector(it) to it }
            .toList()
            .sortedBy { it.first }
            .map { it.second }

    companion object {
        /** The minimum character length for a guild's name. */
        const val NAME_MIN_LENGTH: Int = 2

        /** The maximum character length for a guild's name. */
        const val NAME_MAX_LENGTH: Int = 32

        /** The allowed range of character length for a guild's name. */
        val NAME_LENGTH_RANGE: IntRange = NAME_MIN_LENGTH..NAME_MAX_LENGTH
    }
}

/** Returns the current [BotClient] member of this guild or `null` if the request failed. */
suspend fun Guild.getSelfMember(): GuildMember? = getMember(context.selfUserID)

/** The @everyone [GuildRole] applied to all members for base [Permission] settings. */
val Guild.everyoneRole: GuildRole get() = getRole(id)!!

/**
 * Set the positions of this guild's roles in the guild's role hierarchy using an ordered collection, given by the
 * [orderedCollection] parameter. Any role not included will be appended to the end of the hierarchy, and the last role
 * in the given collection will be at the top of the hierarchy.
 *
 * Returns `true` on success. **Requires [Permission.ManageRoles].**
 */
suspend fun Guild.setRolePositions(orderedCollection: Collection<GuildRole>): Boolean =
    setRolePositions(orderedCollection.map { it.id })

/**
 * Set the positions of these roles in their guild's role hierarchy. The first role in the collection will be at the top
 * of the hierarchy.
 *
 * Returns `true` on success. **Requires [Permission.ManageRoles].**
 */
suspend fun Collection<GuildRole>.setPositions(): Boolean {
    require(isNotEmpty()) { "Collections must contain at least one GuildRole to set positions." }
    require(all { it.guildID == first().guildID }) { "All GuildRoles must be from the same Guild." }
    return first().getGuild().setRolePositions(this)
}

/**
 * A member is a [User] associated with a specific [Guild (aka server)][Guild]. A member object holds user data which is
 * only relevant to the guild in which it resides, such as its nickname.
 */
class GuildMember internal constructor(private val data: GuildMemberData) {
    /** The unique [User ID][User.id] of the backing [User]. */
    val userID = data.user.id

    /** The unique [Guild ID][Guild.id] of the [Guild] this [GuildMember] belongs to. */
    val guildID = data.guild.id

    /** The backing user of this member. */
    suspend fun getUser(): User = data.user.lazyEntity

    /** The guild in which this member resides. */
    suspend fun getGuild(): Guild = data.guild.lazyEntity

    /** The roles that this member belongs to. */
    suspend fun getRoles(): List<GuildRole> = data.roles.map { it.lazyEntity }

    /** The highest ranking role this member has. */
    suspend fun getHighestRole(): GuildRole? = getRoles().asFlow()
        .map { it.getPosition() to it }
        .toList()
        .maxByOrNull { it.first }
        ?.second

    /** An optional nickname, which is used as an alias for this member in the guild. */
    suspend fun getNickname(): String? = data.nickname

    /** The date and time at which this member joined the guild. */
    suspend fun getJoinedAt(): Instant = data.joinedAt

    /** Whether this member is deafened in the guild's voice channels. */
    suspend fun isDeafened(): Boolean = data.isDeafened

    /** Whether this member is muted in the guild's voice channels. */
    suspend fun isMuted(): Boolean = data.isMuted

    /** The [Presence] of this member in the guild. */
    suspend fun getPresence(): Presence? = data.guild.getPresence(data.user.id)

    /** The [VoiceState] of this member in the guild. */
    suspend fun getVoiceState(): VoiceState? = data.guild.getVoiceState(data.user.id)

    /**
     * Set this member's [nickname]. Returns `true` if the nickname was changed.
     * **Requires [Permission.ManageNicknames] or [Permission.ChangeNickname] (if self)**.
     */
    suspend fun setNickname(nickname: String): Boolean {
        val route = if (userID == getGuild().context.selfUserID) Route.ModifyCurrentUserNick(guildID, nickname)
        else Route.ModifyGuildMember(guildID, userID, ModifyGuildMemberPacket(nick = nickname))
        return getGuild().context.requester.sendRequest(route).status.isSuccess()
    }

    /**
     * Give this member the [GuildRole] with this [roleID].
     * Returns `true` if successful. *Requires [Permission.ManageRoles].*
     */
    suspend fun addRole(roleID: Long): Boolean =
        getGuild().context.requester.sendRequest(Route.AddGuildMemberRole(guildID, userID, roleID)).status.isSuccess()

    /**
     * Remove the [GuildRole] with this [roleID] from this member. Returns `true` if successful.
     * *Requires [Permission.ManageRoles].*
     */
    suspend fun removeRole(roleID: Long): Boolean =
        getGuild().context.requester.sendRequest(
            Route.RemoveGuildMemberRole(
                guildID,
                userID,
                roleID
            )
        ).status.isSuccess()

    /**
     * Set whether the member is deafened in [Voice Channels][GuildVoiceChannel].
     * Returns `true` if successful.
     */
    suspend fun setDeafened(deafened: Boolean): Boolean {
        require(this.getVoiceState()?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to set deafen state."
        }
        return getGuild().context.requester
            .sendRequest(Route.ModifyGuildMember(guildID, userID, ModifyGuildMemberPacket(deaf = deafened)))
            .status.isSuccess()
    }

    /**
     * Set whether the member is muted in [Voice Channels][GuildVoiceChannel].
     * Returns `true` if successful.
     */
    suspend fun setMuted(muted: Boolean): Boolean {
        require(this.getVoiceState()?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to set mute state."
        }
        return getGuild().context.requester.sendRequest(
            Route.ModifyGuildMember(guildID, userID, ModifyGuildMemberPacket(mute = muted))
        ).status.isSuccess()
    }

    /** Move the member to another [GuildVoiceChannel]. Requires the member is already in a voice channel. */
    suspend fun move(channelID: Long): Boolean {
        require(this.getVoiceState()?.voiceChannel != null) {
            "GuildMember must be connected to a voice channel to move channels."
        }
        return getGuild().context.requester
            .sendRequest(Route.ModifyGuildMember(guildID, userID, ModifyGuildMemberPacket(channel_id = channelID)))
            .status.isSuccess()
    }

    /** Checks if this guild member is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean =
        other is GuildMember && other.userID == userID && other.guildID == guildID
}

/** Assign the given [role] to this member. Returns `true` if successful. **Requires [Permission.ManageRoles].** */
suspend fun GuildMember.addRole(role: GuildRole): Boolean = addRole(role.id)

/** Remove the given [role] from this member. Returns `true` if successful. **Requires [Permission.ManageRoles].** */
suspend fun GuildMember.removeRole(role: GuildRole): Boolean = removeRole(role.id)

/** Deafen this member in its guild's voice channels. Returns `true` if the action was successful. */
suspend fun GuildMember.deafen(): Boolean = setDeafened(true)

/** Un-deafen this member in its guild's voice channels. Returns `true` if the action was successful. */
suspend fun GuildMember.unDeafen(): Boolean = setDeafened(false)

/** Mute this member in its guild's voice channels. Returns `true` if the action was successful. */
suspend fun GuildMember.mute(): Boolean = setMuted(true)

/** Unmute this member in its guild's voice channels. Returns `true` if the action was successful. */
suspend fun GuildMember.unMute(): Boolean = setMuted(false)

/** Move this member from one voice channel to another. Requires the member to already be in a voice channel. */
suspend fun GuildMember.move(voiceChannel: GuildVoiceChannel): Boolean = move(voiceChannel.id)

/**
 * Represents the action of banning a [User] from a [Guild], and contains relevant information to that ban. This
 * includes the [user] itself, their [ID][userID], and the [reason] (if any) that this user was banned.
 *
 * @property reason The reason for the banning
 * @property userID The [ID][User.id] of the banned [User]
 * @property user The banned [User]
 */
data class GuildBan(val reason: String?, val userID: Long, val user: User)

internal fun BanPacket.toGuildBan(context: BotClient) = GuildBan(reason, user.id, user.toData(context).lazyEntity)

/**
 * Represents a connection between a third-party API and a [Guild]. For examples and more information,
 * [see](https://discordapp.com/streamkit).
 *
 * @property guild The [Guild] which this integration is in
 * @property name The name of the Integration
 * @property type YouTube, Twitch, etc
 * @property enabled Whether this integration is enabled
 * @property syncing Whether this integration is being synchronized
 * @property role The [GuildRole] this integration uses for subscribers
 * @property expireBehavior The behavior for expiring subscribers
 * @property gracePeriod The grace period (in days) before a subscriber is expired
 * @property member The member which "owns" this integration
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
    val lastSync: Instant
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
        /** Remove the [role] from the member when their subscription expires. */
        REMOVE_ROLE,

        /** Kick the member when their subscription expires. */
        KICK
    }

    /** Set the [expireBehavior]. Returns `true` if set successfully. */
    suspend fun setExpireBehavior(behavior: ExpireBehavior): Boolean = context.requester.sendRequest(
        Route.ModifyGuildIntegration(
            guild.id,
            id,
            ModifyGuildIntegrationPacket(behavior.ordinal, gracePeriod, emojiEnabled)
        )
    ).status.isSuccess()
        .also { if (it) this.expireBehavior = behavior }

    /** Set the [gracePeriod]. Returns `true` if set successfully. Must be 1, 3, 7, 14, or 30 days. */
    suspend fun setGracePeriod(days: Int): Boolean {
        require(days in listOf(1, 3, 7, 14, 30)) { "Grace Period must be 1, 3, 7, 14, or 30 days." }
        return context.requester.sendRequest(
            Route.ModifyGuildIntegration(
                guild.id,
                id,
                ModifyGuildIntegrationPacket(expireBehavior.ordinal, days, emojiEnabled)
            )
        ).status.isSuccess()
            .also { if (it) this.gracePeriod = days }
    }

    /** Set [emojiEnabled]. Returns `true` if set successfully. */
    suspend fun setEmojiEnabled(enabled: Boolean): Boolean = context.requester.sendRequest(
        Route.ModifyGuildIntegration(
            guild.id,
            id,
            ModifyGuildIntegrationPacket(expireBehavior.ordinal, gracePeriod, enabled)
        )
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
 * @property approxPresences Approximate count of online members.
 * @property approxMemberCount Approximate count of total members.
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
    val activeTimeRange: ClosedRange<Instant>,
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
 * Whether members who have not explicitly set their notification settings will receive a notification for every
 * [message][Message] in this guild.
 */
enum class MessageNotificationLevel {
    /** A notification will be sent on each message. */
    ALL_MESSAGES,

    /** A notification will be sent ONLY when the member is mentioned. */
    ONLY_MENTIONS
}

/**
 * How broadly, if at all, messages will be filtered for explicit content.
 */
enum class ExplicitContentFilterLevel {
    /** Discord will not scan any messages. */
    DISABLED,

    /** Discord will scan messages from any member without a [GuildRole]. */
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
 * The verification criteria needed for users to send a [Message] either within a [Guild] or directly to any member in a
 * [Guild].
 */
enum class VerificationLevel {
    /** No verification required. */
    NONE,

    /** Must have a verified email. */
    LOW,

    /** [LOW] + must be registered on Discord for longer than 5 minutes. */
    MEDIUM,

    /** [MEDIUM] + must be a member of this guild for longer than 10 minutes. */
    HIGH,

    /** [HIGH] + must have a verified phone on their Discord account. */
    VERY_HIGH
}

/** Widget style types used when [retrieving a Guild's widget PNG][Guild.getGuildWidgetPng] */
enum class GuildWidgetStyle(internal val urlParam: String) {
    /**
     * Shield style widget with Discord icon and guild members online count.
     * [example](https://discord.com/api/guilds/81384788765712384/widget.png?style=shield)
     */
    SHIELD("shield"),

    /**
     * large image with guild icon, name and online count. "POWERED BY DISCORD" as the footer of the widget.
     * [example](https://discord.com/api/guilds/81384788765712384/widget.png?style=banner1)
     */
    BANNER_1("banner1"),

    /**
     * smaller widget style with guild icon, name and online count. Split on the right with Discord logo
     * [example](https://discord.com/api/guilds/81384788765712384/widget.png?style=banner2)
     */
    BANNER_2("banner2"),

    /**
     * large image with guild icon, name and online count. In the footer, Discord logo on the left and "Chat Now" on the right
     * [example](https://discord.com/api/guilds/81384788765712384/widget.png?style=banner3)
     */
    BANNER_3("banner3"),

    /**
     * large Discord logo at the top of the widget. Guild icon, name and online count in the middle portion of
     * the widget and a "JOIN MY SERVER" button at the bottom
     * [example](https://discord.com/api/guilds/81384788765712384/widget.png?style=banner4)
     */
    BANNER_4("banner4");
}
