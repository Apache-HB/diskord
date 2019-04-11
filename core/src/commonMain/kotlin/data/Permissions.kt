package com.serebit.strife.data

import com.serebit.strife.internal.packets.PermissionOverwritePacket

enum class PermissionType { GENERAL, TEXT, VOICE }

sealed class Permission(internal val bitOffset: Int, val type: PermissionType) {
    /**
     * Allows a guild member to create invites for others to join the guild. In a newly created guild, members
     * have this permission by default.
     */
    object CreateInstantInvite : Permission(1 shl 0, PermissionType.GENERAL)

    /**
     * Allows a guild member to forcibly remove other guild members from the guild. Kicking a user does not
     * prevent the kicked user from returning to the guild.
     */
    object KickMembers : Permission(1 shl 1, PermissionType.GENERAL)

    /**
     * Allows a guild member to forcibly remove other guild members from the guild, and lock them from joining
     * back until such time when the ban is lifted.
     */
    object BanMembers : Permission(1 shl 2, PermissionType.GENERAL)

    /**
     * Equivalent to giving a guild member every permission. This also bypasses per-channel permission
     * overrides. Only give this permission to people (or bots) that you trust!
     */
    object Administrator : Permission(1 shl 3, PermissionType.GENERAL)

    /**
     * Allows a guild member to manage and edit voice and text channels, along with channel categories. This
     * includes changing names, changing topics, sorting, and changing channel permission overrides.
     */
    object ManageChannels : Permission(1 shl 4, PermissionType.GENERAL)

    /** Allows a guild member to change the guild's settings, including the guild's name, icon, et cetera. */
    object ManageGuild : Permission(1 shl 5, PermissionType.GENERAL)

    /**
     * Allows a guild member to view the guild's audit log, which is a comprehensive list of all administrative
     * actions taken by members of the guild. This includes (but is not limited to) kicks, bans, message
     * deletions, and nickname changes.
     */
    object ViewAuditLog : Permission(1 shl 7, PermissionType.GENERAL)

    /**
     * Allows a guild member to view text channels and voice channels. In a newly created guild, members have
     * this permission by default.
     */
    object ViewChannels : Permission(1 shl 10, PermissionType.GENERAL)

    /**
     * Allows a guild member to change their own nickname. In a newly created guild, members have this permission
     * by default.
     */
    object ChangeNickname : Permission(1 shl 26, PermissionType.GENERAL)

    /**
     * Allows a guild member to change the nicknames of other members, so long as those members are below them in
     * the role hierarchy.
     */
    object ManageNicknames : Permission(1 shl 27, PermissionType.GENERAL)

    /**
     * Allows a guild member to manage, edit, and assign roles, so long as those roles are below them in the role
     * hierarchy.
     */
    object ManageRoles : Permission(1 shl 28, PermissionType.GENERAL)

    /** Allows a guild member to add, edit, and remove webhooks from the guild. */
    object ManageWebhooks : Permission(1 shl 29, PermissionType.GENERAL)

    /** Allows a guild member to add, edit, and remove custom emotes from the guild. */
    object ManageEmotes : Permission(1 shl 30, PermissionType.GENERAL)

    object AddReactions : Permission(1 shl 6, PermissionType.TEXT)
    object SendMessages : Permission(1 shl 11, PermissionType.TEXT)
    object SendTtsMessages : Permission(1 shl 12, PermissionType.TEXT)
    object ManageMessages : Permission(1 shl 13, PermissionType.TEXT)
    object EmbedLinks : Permission(1 shl 14, PermissionType.TEXT)
    object AttachFiles : Permission(1 shl 15, PermissionType.TEXT)
    object ReadMessageHistory : Permission(1 shl 16, PermissionType.TEXT)
    object MentionEveryone : Permission(1 shl 17, PermissionType.TEXT)
    object UseExternalEmotes : Permission(1 shl 18, PermissionType.TEXT)

    object Connect : Permission(1 shl 20, PermissionType.VOICE)
    object Speak : Permission(1 shl 21, PermissionType.VOICE)
    object MuteMembers : Permission(1 shl 22, PermissionType.VOICE)
    object DeafenMembers : Permission(1 shl 23, PermissionType.VOICE)
    object MoveMembers : Permission(1 shl 24, PermissionType.VOICE)
    object UseVoiceActivity : Permission(1 shl 25, PermissionType.VOICE)
    object PrioritySpeaker : Permission(1 shl 8, PermissionType.VOICE)

    companion object {
        val values = setOf(
            CreateInstantInvite, KickMembers, BanMembers, Administrator, ManageChannels, ManageGuild, ViewAuditLog,
            ViewChannels, ChangeNickname, ManageNicknames, ManageRoles, ManageWebhooks, ManageEmotes,

            AddReactions, SendMessages, SendTtsMessages, ManageMessages, EmbedLinks, AttachFiles, ReadMessageHistory,
            MentionEveryone, UseExternalEmotes,

            Connect, Speak, MuteMembers, DeafenMembers, MoveMembers, UseVoiceActivity, PrioritySpeaker
        )
    }
}

internal fun Int.toPermissions() = Permission.values.filter { it.bitOffset and this != 0 }.toSet()

internal fun Collection<Permission>.toBitSet() = fold(0) { acc, it -> acc or it.bitOffset }

sealed class PermissionOverride {
    abstract val id: Long
    abstract val allow: Set<Permission>
    abstract val deny: Set<Permission>
}

data class RolePermissionOverride(
    override val id: Long,
    override val allow: Set<Permission>,
    override val deny: Set<Permission>
) : PermissionOverride()

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
