package com.serebit.diskord.data

import com.serebit.diskord.BitSet

sealed class Permission(internal val bitOffset: Int) {
    sealed class General(bitOffset: Int) : Permission(bitOffset) {
        /**
         * Allows a guild member to create invites for others to join the guild. In a newly created guild, members
         * have this permission by default.
         */
        object CreateInstantInvite : General(Offsets.createInstantInvite)

        /**
         * Allows a guild member to forcibly remove other guild members from the guild. Kicking a user does not
         * prevent the kicked user from returning to the guild.
         */
        object KickMembers : General(Offsets.kickMembers)

        /**
         * Allows a guild member to forcibly remove other guild members from the guild, and lock them from joining
         * back until such time when the ban is lifted.
         */
        object BanMembers : General(Offsets.banMembers)

        /**
         * Equivalent to giving a guild member every permission. This also bypasses per-channel permission
         * overrides. Only give this permission to people (or bots) that you trust!
         */
        object Administrator : General(Offsets.administrator)

        /**
         * Allows a guild member to manage and edit voice and text channels, along with channel categories. This
         * includes changing names, changing topics, sorting, and changing channel permission overrides.
         */
        object ManageChannels : General(Offsets.manageChannels)

        /**
         * Allows a guild member to change the guild's settings, including the guild's name, icon, et cetera.
         */
        object ManageGuild : General(Offsets.manageGuild)

        /**
         * Allows a guild member to view the guild's audit log, which is a comprehensive list of all administrative
         * actions taken by members of the guild. This includes (but is not limited to) kicks, bans, message
         * deletions, and nickname changes.
         */
        object ViewAuditLog : General(Offsets.viewAuditLog)

        /**
         * Allows a guild member to view text channels and voice channels. In a newly created guild, members have
         * this permission by default.
         */
        object ViewChannels : General(Offsets.viewChannels)

        /**
         * Allows a guild member to change their own nickname. In a newly created guild, members have this permission
         * by default.
         */
        object ChangeNickname : General(Offsets.changeNickname)

        /**
         * Allows a guild member to change the nicknames of other members, so long as those members are below them in
         * the role hierarchy.
         */
        object ManageNicknames : General(Offsets.manageNicknames)

        /**
         * Allows a guild member to manage, edit, and assign roles, so long as those roles are below them in the role
         * hierarchy.
         */
        object ManageRoles : General(Offsets.manageRoles)

        /**
         * Allows a guild member to add, edit, and remove webhooks from the guild.
         */
        object ManageWebhooks : General(Offsets.manageWebhooks)

        /**
         * Allows a guild member to add, edit, and remove custom emotes from the guild.
         */
        object ManageEmotes : General(Offsets.manageEmotes)

        companion object {
            val values = setOf(
                CreateInstantInvite, KickMembers, BanMembers, Administrator, ManageChannels,
                ManageGuild, ViewAuditLog, ViewChannels, ChangeNickname, ManageNicknames, ManageRoles,
                ManageWebhooks, ManageEmotes
            )
        }
    }

    sealed class Text(bitOffset: Int) : Permission(bitOffset) {
        object AddReactions : Text(Offsets.addReactions)
        object SendMessages : Text(Offsets.sendMessages)
        object SendTtsMessages : Text(Offsets.sendTtsMessages)
        object ManageMessages : Text(Offsets.manageMessages)
        object EmbedLinks : Text(Offsets.embedLinks)
        object AttachFiles : Text(Offsets.attachFiles)
        object ReadMessageHistory : Text(Offsets.readMessageHistory)
        object MentionEveryone : Text(Offsets.mentionEveryone)
        object UseExternalEmotes : Text(Offsets.useExternalEmotes)

        companion object {
            val values = setOf(
                AddReactions, SendMessages, SendTtsMessages, ManageMessages, EmbedLinks, AttachFiles,
                ReadMessageHistory, MentionEveryone, UseExternalEmotes
            )
        }
    }

    sealed class Voice(bitOffset: Int) : Permission(bitOffset) {
        object Connect : Voice(Offsets.connect)
        object Speak : Voice(Offsets.speak)
        object MuteMembers : Voice(Offsets.muteMembers)
        object DeafenMembers : Voice(Offsets.deafenMembers)
        object MoveMembers : Voice(Offsets.moveMembers)
        object UseVoiceActivity : Voice(Offsets.useVoiceActivity)
        object PrioritySpeaker : Voice(Offsets.prioritySpeaker)

        companion object {
            val values =
                setOf(Connect, Speak, MuteMembers, DeafenMembers, MoveMembers, UseVoiceActivity, PrioritySpeaker)
        }
    }

    private object Offsets {
        const val createInstantInvite = 1 shl 0
        const val kickMembers = 1 shl 1
        const val banMembers = 1 shl 2
        const val administrator = 1 shl 3
        const val manageChannels = 1 shl 4
        const val manageGuild = 1 shl 5
        const val addReactions = 1 shl 6
        const val viewAuditLog = 1 shl 7
        const val prioritySpeaker = 1 shl 8
        const val viewChannels = 1 shl 10
        const val sendMessages = 1 shl 11
        const val sendTtsMessages = 1 shl 12
        const val manageMessages = 1 shl 13
        const val embedLinks = 1 shl 14
        const val attachFiles = 1 shl 15
        const val readMessageHistory = 1 shl 16
        const val mentionEveryone = 1 shl 17
        const val useExternalEmotes = 1 shl 18
        const val connect = 1 shl 20
        const val speak = 1 shl 21
        const val muteMembers = 1 shl 22
        const val deafenMembers = 1 shl 23
        const val moveMembers = 1 shl 24
        const val useVoiceActivity = 1 shl 25
        const val changeNickname = 1 shl 26
        const val manageNicknames = 1 shl 27
        const val manageRoles = 1 shl 28
        const val manageWebhooks = 1 shl 29
        const val manageEmotes = 1 shl 30
    }

    companion object {
        val values = General.values + Text.values + Voice.values
    }
}

fun BitSet.toPermissions(): List<Permission> = Permission.values.filter { it.bitOffset and this != 0 }
