package com.serebit.diskord.data

import com.serebit.diskord.BitSet

sealed class Permission(internal val bitOffset: Int) {
    sealed class General(bitOffset: Int) : Permission(bitOffset) {
        object CreateInstantInvite : General(Offsets.createInstantInvite)
        object KickMembers : General(Offsets.kickMembers)
        object BanMembers : General(Offsets.banMembers)
        object Administrator : General(Offsets.administrator)
        object ManageChannels : General(Offsets.manageChannels)
        object ManageServer : General(Offsets.manageServer)
        object ViewAuditLog : General(Offsets.viewAuditLog)
        object ViewChannels : General(Offsets.viewChannels)
        object ChangeNickname : General(Offsets.changeNickname)
        object ManageNicknames : General(Offsets.manageNicknames)
        object ManageRoles : General(Offsets.manageRoles)
        object ManageWebhooks : General(Offsets.manageWebhooks)
        object ManageEmotes : General(Offsets.manageEmotes)

        companion object {
            val values = setOf(
                CreateInstantInvite, KickMembers, BanMembers, Administrator, ManageChannels,
                ManageServer, ViewAuditLog, ViewChannels, ChangeNickname, ManageNicknames, ManageRoles,
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

        companion object {
            val values = setOf(Connect, Speak, MuteMembers, DeafenMembers, MoveMembers, UseVoiceActivity)
        }
    }

    private object Offsets {
        const val createInstantInvite = 1
        const val kickMembers = 1 shl 1
        const val banMembers = 1 shl 2
        const val administrator = 1 shl 3
        const val manageChannels = 1 shl 4
        const val manageServer = 1 shl 5
        const val addReactions = 1 shl 6
        const val viewAuditLog = 1 shl 7
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
        private val values = General.values + Text.values + Voice.values

        internal fun from(bitSet: BitSet) = values.filter { it.bitOffset and bitSet != 0 }
    }
}
