package com.serebit.diskord.data

import com.serebit.diskord.BitSet

@Suppress("MagicNumber")
sealed class Permission(internal val bitOffset: Long) {
    sealed class General(bitOffset: Long) : Permission(bitOffset) {
        object CreateInstantInvite : General(1L)
        object KickMembers : General(2L)
        object BanMembers : General(4L)
        object Administrator : General(8L)
        object ManageChannels : General(16L)
        object ManageServer : General(32L)
        object ViewAuditLog : General(128L)
        object ViewChannels : General(1024L)
        object ChangeNickname : General(67108864L)
        object ManageNicknames : General(134217728L)
        object ManageRoles : General(268435456L)
        object ManageWebhooks : General(536870912L)
        object ManageEmotes : General(1073741824L)

        companion object {
            val values = setOf(
                CreateInstantInvite, KickMembers, BanMembers, Administrator, ManageChannels,
                ManageServer, ViewAuditLog, ViewChannels, ChangeNickname, ManageNicknames, ManageRoles,
                ManageWebhooks, ManageEmotes
            )
        }
    }

    sealed class Text(bitOffset: Long) : Permission(bitOffset) {
        object AddReactions : Text(64L)
        object SendMessages : Text(2048L)
        object SendTtsMessages : Text(4096L)
        object ManageMessages : Text(8192L)
        object EmbedLinks : Text(16384L)
        object AttachFiles : Text(32768L)
        object ReadMessageHistory : Text(65536L)
        object MentionEveryone : Text(131072L)
        object UseExternalEmotes : Text(262144L)

        companion object {
            val values = setOf(
                AddReactions, SendMessages, SendTtsMessages, ManageMessages, EmbedLinks, AttachFiles,
                ReadMessageHistory, MentionEveryone, UseExternalEmotes
            )
        }
    }

    sealed class Voice(bitOffset: Long) : Permission(bitOffset) {
        object Connect : Voice(1048576L)
        object Speak : Voice(2097152L)
        object MuteMembers : Voice(4194304L)
        object DeafenMembers : Voice(8388608L)
        object MoveMembers : Voice(16777216L)
        object UseVoiceActivity : Voice(33554432L)

        companion object {
            val values = setOf(Connect, Speak, MuteMembers, DeafenMembers, MoveMembers, UseVoiceActivity)
        }
    }

    companion object {
        private val values = General.values + Text.values + Voice.values

        internal fun from(bitSet: BitSet) = values.filter { it.bitOffset and bitSet != 0L }
    }
}
