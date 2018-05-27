package com.serebit.diskord.data

import java.awt.Color

/*
fun roleConverter(snowflake: Int): RolePermissions {
    //TODO: rigure out how to convert snowflakes to roles
}
*/

data class RoleData(
    val name: String,
    val id: Long,
    val guild_id: Long,
    val managed: Boolean,
    val colour: Color,
    val hoist: Boolean,
    val position: Int,
    val mentionable: Boolean,
    val permissions: RolePermissions) {

    fun getMention(): String {
        return "<@$id>"
    }
}

data class RolePermissions(
    val general_permissions: GeneralPermissions,
    val text_permissions: TextPermissions,
    val voice_permissions: VoicePermissions
)

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
    val mamage_messages: Boolean,
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