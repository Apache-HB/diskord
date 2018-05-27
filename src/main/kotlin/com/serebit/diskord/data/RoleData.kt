package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

data class RoleData(
    val id: Snowflake,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: Int,
    val managed: Boolean,
    val mentionable: Boolean
)



object PermissionBits {

    //https://discordapp.com/developers/tools/permissions-calculator
    //this was used as reference to get these values, go here again if they ever update

    const val administrator = 8
    const val view_audit_log = 128
    const val manage_server = 32
    const val manage_roles = 268435456
    const val manage_channels = 16
    const val kick_members = 2
    const val ban_members = 4
    const val create_instant_invite = 1
    const val change_nickname = 67108864
    const val manage_nicknames = 134217728
    const val manage_emojis = 1073741824
    const val manage_webhooks = 536870912
    const val view_channels = 1024

    const val send_messages = 2048
    const val send_tts_messages = 4096
    const val manage_messages = 8192
    const val embed_links = 16384
    const val attach_files = 32768
    const val read_message_history = 65536
    const val mention_everyone = 131072
    const val use_external_emojis = 262144
    const val add_reactions = 64

    const val connect = 1048576
    const val speak = 2097152
    const val mute_members = 4194304
    const val deafen_members = 8388608
    const val use_members = 16777216
    const val use_voice_activity = 33554432
}

data class RolePermissions(
    val general: GeneralPermissions,
    val text: TextPermissions,
    val voice: VoicePermissions) {

    companion object {

        fun from(snowflake: Long) = GeneralPermissions(
                    snowflake and PermissionBits.administrator == PermissionBits.administrator,
                    snowflake and PermissionBits.view_audit_log == PermissionBits.view_audit_log,
                    snowflake and PermissionBits.manage_server == PermissionBits.manage_server,
                    snowflake and PermissionBits.manage_roles == PermissionBits.manage_roles,
                    snowflake and PermissionBits.manage_channels == PermissionBits.manage_channels,
                    snowflake and PermissionBits.kick_members == PermissionBits.kick_members,
                    snowflake and PermissionBits.ban_members == PermissionBits.ban_members,
                    snowflake and PermissionBits.create_instant_invite == PermissionBits.create_instant_invite,
                    snowflake and PermissionBits.change_nickname == PermissionBits.change_nickname,
                    snowflake and PermissionBits.manage_nicknames == PermissionBits.manage_nicknames,
                    snowflake and PermissionBits.manage_emojis == PermissionBits.manage_emojis,
                    snowflake and PermissionBits.manage_webhooks == PermissionBits.manage_webhooks,
                    snowflake and PermissionBits.view_channels == PermissionBits.view_channels),

                TextPermissions(
                    snowflake and PermissionBits.send_messages == PermissionBits.send_messages,
                    snowflake and PermissionBits.send_tts_messages == PermissionBits.send_tts_messages,
                    snowflake and PermissionBits.manage_messages == PermissionBits.manage_messages,
                    snowflake and PermissionBits.embed_links == PermissionBits.embed_links,
                    snowflake and PermissionBits.attach_files == PermissionBits.attach_files,
                    snowflake and PermissionBits.read_message_history == PermissionBits.read_message_history,
                    snowflake and PermissionBits.mention_everyone == PermissionBits.mention_everyone,
                    snowflake and PermissionBits.use_external_emojis == PermissionBits.use_external_emojis,
                    snowflake and PermissionBits.add_reactions == PermissionBits.add_reactions),

                VoicePermissions(
                    snowflake and PermissionBits.connect == PermissionBits.connect,
                    snowflake and PermissionBits.speak == PermissionBits.speak,
                    snowflake and PermissionBits.mute_members == PermissionBits.mute_members,
                    snowflake and PermissionBits.deafen_members == PermissionBits.deafen_members,
                    snowflake and PermissionBits.use_members == PermissionBits.use_members,
                    snowflake and PermissionBits.use_voice_activity == PermissionBits.use_voice_activity))

    }
}

data class GeneralPermissions(
    val administrator: Boolean,
    val view_audit_log: Boolean,
    val manage_server: Boolean,
    val manage_roles: Boolean,
    val manage_channels: Boolean,
    val kick_members: Boolean,
    val ban_members: Boolean,
    val create_instant_invite: Boolean,
    val change_nickname: Boolean,
    val manage_nicknames: Boolean,
    val manage_emojis: Boolean,
    val manage_webhooks: Boolean,
    val view_channels: Boolean
)

data class TextPermissions(
    val send_messages: Boolean,
    val send_tts_messages: Boolean,
    val manage_messages: Boolean,
    val embed_links: Boolean,
    val attach_files: Boolean,
    val read_message_history: Boolean,
    val mention_everyone: Boolean,
    val use_external_emojis: Boolean,
    val add_reactions: Boolean
)

data class VoicePermissions(
    val connect: Boolean,
    val speak: Boolean,
    val mute_members: Boolean,
    val deafen_members: Boolean,
    val use_members: Boolean,
    val use_voice_activity: Boolean
)