package com.serebit.strife.data

import com.serebit.strife.data.PermissionType.*
import com.serebit.strife.entities.*
import com.serebit.strife.internal.packets.PermissionOverwritePacket

/** The target of a [Permission] within a [Guild]. */
enum class PermissionType {
    /** A general permission. */
    GENERAL,
    /** A permission for a [TextChannel] */
    TEXT,
    /** A permission for a [GuildVoiceChannel] */
    VOICE
}

/**
 * [Permissions][Permission] in Discord are a way to control [GuildMember] access to certain
 * abilities within a [Guild]. A set of base [permissions][Permission] can be configured
 * at the [Guild] level for different [roles][GuildRole]. When these [roles][GuildRole] are attached
 * to a [GuildMember], they grant or revoke specific privileges within the [Guild]. Along with
 * the guild-level permissions, Discord also supports permission overwrites that can be
 * assigned to individual [roles][GuildRole] or [members][GuildMember] on a per-[Channel] basis.
 *
 * "[Permissions][Permission] are stored within a 53-bit [Int] and are calculated using
 * bitwise operations. The total permissions integer can be determined by OR-ing together
 * each individual value, and flags can be checked using AND operations."
 *
 * [see](https://discordapp.com/developers/docs/topics/permissions#permissions)
 *
 * @property bitOffset the bitwise shift needed to read a [Permission] from the API 53-[Int] representation
 * @property type The target of the [Permission].
 */
enum class Permission(internal val bitOffset: Int, val type: PermissionType) {
    /**
     * Allows a guild member to create invites for others to join the guild. In a newly created guild, members have
     * this permission by default.
     */
    CreateInstantInvite(1 shl 0, GENERAL),
    /**
     * Allows a guild member to forcibly remove other guild members from the guild. Kicking a user does not prevent
     * the kicked user from returning to the guild.
     */
    KickMembers(1 shl 1, GENERAL),
    /**
     * Allows a guild member to forcibly remove other guild members from the guild, and lock them from joining
     * back until such time when the ban is lifted.
     */
    BanMembers(1 shl 2, GENERAL),
    /**
     * Equivalent to giving a guild member every permission. This also bypasses per-channel permission
     * overrides. Only give this permission to people (or bots) that you trust!
     */
    Administrator(1 shl 3, GENERAL),
    /**
     * Allows a guild member to manage and edit voice and text channels, along with channel categories. This
     * includes changing names, changing topics, sorting, and changing channel permission overrides.
     */
    ManageChannels(1 shl 4, GENERAL),
    /** Allows a guild member to change the guild's settings, including the guild's name, icon, et cetera. */
    ManageGuild(1 shl 5, GENERAL),
    /**
     * Allows a guild member to view the guild's audit log, which is a comprehensive list of all administrative
     * actions taken by members of the guild. This includes (but is not limited to) kicks, bans, message
     * deletions, and nickname changes.
     */
    ViewAuditLog(1 shl 7, GENERAL),
    /**
     * Allows a guild member to view text channels and voice channels. In a newly created guild, members have
     * this permission by default.
     */
    ViewChannels(1 shl 10, GENERAL),
    /**
     * Allows a member to change their own nickname. In a newly created guild, members have this permission by
     * default.
     */
    ChangeNickname(1 shl 26, GENERAL),
    /** Allows a member to change the nicknames of members they outrank in the [GuildRole] hierarchy. */
    ManageNicknames(1 shl 27, GENERAL),
    /** Allows a member to manage, edit, & assign roles, given those roles are below them in the hierarchy. */
    ManageRoles(1 shl 28, GENERAL),
    /** Allows a member to add, edit, and remove webhooks from the guild. */
    ManageWebhooks(1 shl 29, GENERAL),
    /** Allows a member to add, edit, and remove [custom emojis][GuildEmoji] from the guild. */
    ManageEmojis(1 shl 30, GENERAL),

    /** Allows for the addition of reactions to messages. */
    AddReactions(1 shl 6, TEXT),
    /** Allows for sending messages in a [TextChannel]. **This is overridden by [ViewChannels].** */
    SendMessages(1 shl 11, TEXT),
    /** Allows for sending Text-to-Speech messages in a [TextChannel]. **This is overridden by [ViewChannels].** */
    SendTtsMessages(1 shl 12, TEXT),
    /** Allows for deletion of any [Message] ina [TextChannel]. */
    ManageMessages(1 shl 13, TEXT),
    /**
     * If a member has this permission, links that they send to chat may produce an embed showing information about
     * the linked website.
     */
    EmbedLinks(1 shl 14, TEXT),
    /** Allows for the uploading and sharing of files to chat. */
    AttachFiles(1 shl 15, TEXT),
    /** Allows a member to read message history from before their current Discord session. */
    ReadMessageHistory(1 shl 16, TEXT),
    /**
     * Allows a member to send `@everyone` (which sends a notification to every member of the server where the
     * message was sent), and `@here` (which sends a notification to every currently-logged-in member of the server
     * where the message was sent) pings. Members can still send these strings to chat, but other server members will
     * only be pinged if the message author has this permission.
     */
    MentionEveryone(1 shl 17, TEXT),
    /**
     * Allows a member to use emojis from other Discord servers. This functionality is generally only available to
     * Discord Nitro users, or via integration with another service.
     */
    UseExternalEmojis(1 shl 18, TEXT),

    /** Allows a member to connect to voice channels. */
    Connect(1 shl 20, VOICE),
    /** Allows a member to speak in voice channels. */
    Speak(1 shl 21, VOICE),
    /** Allows a member to mute other members in voice channels. */
    MuteMembers(1 shl 22, VOICE),
    /** Allows a member to deafen other members in voice channels. */
    DeafenMembers(1 shl 23, VOICE),
    /** Allows a member to move other members between voice channels. */
    MoveMembers(1 shl 24, VOICE),
    /**
     * Allows a member to set their Discord client to only send audio from their mic to a voice channel when it
     * detects that they are speaking. If a member doesn't have this permission, they must use push-to-talk in voice
     * channels.
     */
    UseVoiceActivity(1 shl 25, VOICE),
    /**
     * Gives a member the ability to talk over others in voice channels by lowering the volume of other speakers. A
     * special keybind must be set in the Discord client to use this feature.
     */
    PrioritySpeaker(1 shl 8, VOICE);
}

/** Convert a permission int value to a usable [Permission]. */
internal fun Int.toPermissions() = Permission.values().filter { it.bitOffset and this != 0 }.toSet()

internal fun Collection<Permission>.toBitSet() = fold(0) { acc, it -> acc or it.bitOffset }

/**
 * A permission override is a value assigned to a [TextChannel] that dictates what the associated [User] or [GuildRole]
 * is allowed to do, or disallowed to do. These values override whatever permissions that [User] or [GuildRole]
 * normally has.
 */
sealed class PermissionOverride {
    /** The associated user/role ID of this override. */
    abstract val id: Long
    /** Which permissions are set to be allowed. */
    abstract val allow: Set<Permission>
    /** Which permissions are set to be denied. */
    abstract val deny: Set<Permission>
}

/** A permission override for a [GuildRole] with the given [id]. */
data class RolePermissionOverride(
    override val id: Long,
    override val allow: Set<Permission>,
    override val deny: Set<Permission>
) : PermissionOverride()

/** A permission override for a [User] with the given [id]. */
data class MemberPermissionOverride(
    override val id: Long,
    override val allow: Set<Permission>,
    override val deny: Set<Permission>
) : PermissionOverride()

internal fun PermissionOverwritePacket.toOverride() = when (type) {
    "role" -> RolePermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    "member" -> MemberPermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    else -> null
}

internal fun Iterable<PermissionOverwritePacket>.toOverrides() = mapNotNull { it.toOverride() }
