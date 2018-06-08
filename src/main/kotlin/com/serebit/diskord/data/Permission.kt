package com.serebit.diskord.data

import com.serebit.diskord.BitSet

sealed class Permission(internal val bitOffset: Int) {
    sealed class General(bitOffset: Int) : Permission(bitOffset) {
        object CreateInstantInvite : General(1)
        object KickMembers : General(1 shl 1)
        object BanMembers : General(1 shl 2)
        object Administrator : General(1 shl 3)
        object ManageChannels : General(1 shl 4)
        object ManageServer : General(1 shl 5)
        object ViewAuditLog : General(1 shl 7)
        object ViewChannels : General(1 shl 10)
        object ChangeNickname : General(1 shl 26)
        object ManageNicknames : General(1 shl 27)
        object ManageRoles : General(1 shl 28)
        object ManageWebhooks : General(1 shl 29)
        object ManageEmotes : General(1 shl 30)

        companion object {
            val values = setOf(
                CreateInstantInvite, KickMembers, BanMembers, Administrator, ManageChannels,
                ManageServer, ViewAuditLog, ViewChannels, ChangeNickname, ManageNicknames, ManageRoles,
                ManageWebhooks, ManageEmotes
            )
        }
    }

    sealed class Text(bitOffset: Int) : Permission(bitOffset) {
        object AddReactions : Text(1 shl 6)
        object SendMessages : Text(1 shl 11)
        object SendTtsMessages : Text(1 shl 12)
        object ManageMessages : Text(1 shl 13)
        object EmbedLinks : Text(1 shl 14)
        object AttachFiles : Text(1 shl 15)
        object ReadMessageHistory : Text(1 shl 16)
        object MentionEveryone : Text(1 shl 17)
        object UseExternalEmotes : Text(1 shl 18)

        companion object {
            val values = setOf(
                AddReactions, SendMessages, SendTtsMessages, ManageMessages, EmbedLinks, AttachFiles,
                ReadMessageHistory, MentionEveryone, UseExternalEmotes
            )
        }
    }

    sealed class Voice(bitOffset: Int) : Permission(bitOffset) {
        object Connect : Voice(1 shl 20)
        object Speak : Voice(1 shl 21)
        object MuteMembers : Voice(1 shl 22)
        object DeafenMembers : Voice(1 shl 23)
        object MoveMembers : Voice(1 shl 24)
        object UseVoiceActivity : Voice(1 shl 25)

        companion object {
            val values = setOf(Connect, Speak, MuteMembers, DeafenMembers, MoveMembers, UseVoiceActivity)
        }
    }

    companion object {
        private val values = General.values + Text.values + Voice.values

        internal fun from(bitSet: BitSet) = values.filter { it.bitOffset and bitSet != 0 }
    }
}
