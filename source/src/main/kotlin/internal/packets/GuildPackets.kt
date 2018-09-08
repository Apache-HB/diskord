package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp

internal data class GuildCreatePacket(
    val id: Long,
    var name: String,
    var roles: List<RolePacket>,
    var icon: String?,
    var splash: String?,
    var owner: Boolean?,
    var owner_id: Long,
    var permissions: BitSet?,
    var region: String,
    var afk_channel_id: Long?,
    var afk_timeout: Int,
    var embed_enabled: Boolean?,
    var embed_channel_id: Long?,
    var verification_level: Int,
    var default_message_notifications: Int,
    var explicit_content_filter: Int,
    var emojis: List<EmotePacket>,
    var features: List<String>,
    var mfa_level: Int,
    var application_id: Long?,
    var widget_enabled: Boolean?,
    var widget_channel_id: Long?,
    var system_channel_id: Long?,
    val joined_at: IsoTimestamp,
    val large: Boolean?,
    val unavailable: Boolean?,
    val member_count: Int?,
    var voice_states: List<VoiceStatePacket>,
    var members: List<MemberPacket>,
    var channels: List<GuildChannelPacket>,
    var presences: List<PresencePacket>
) {
    fun update(packet: GuildPacket): GuildCreatePacket {
        name = packet.name
        roles = packet.roles
        icon = packet.icon
        splash = packet.splash
        owner = packet.owner
        owner_id = packet.owner_id
        permissions = packet.permissions
        region = packet.region
        afk_channel_id = packet.afk_channel_id
        afk_timeout = packet.afk_timeout
        embed_enabled = packet.embed_enabled
        embed_channel_id = packet.embed_channel_id
        verification_level = packet.verification_level
        default_message_notifications = packet.default_message_notifications
        explicit_content_filter = packet.explicit_content_filter
        emojis = packet.emojis
        features = packet.features
        mfa_level = packet.mfa_level
        application_id = packet.application_id
        widget_enabled = packet.widget_enabled
        widget_channel_id = packet.widget_channel_id
        system_channel_id = packet.system_channel_id
        return this
    }
}

/**
 * https://discordapp.com/developers/docs/resources/guild#guild-object
 */
internal data class GuildPacket(
    val id: Long,
    val name: String,
    val roles: List<RolePacket>,
    val icon: String?,
    val splash: String?,
    val owner: Boolean?,
    val owner_id: Long,
    val permissions: BitSet?,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Int,
    val embed_enabled: Boolean?,
    val embed_channel_id: Long?,
    val verification_level: Int,
    val default_message_notifications: Int,
    val explicit_content_filter: Int,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Int,
    val application_id: Long?,
    val widget_enabled: Boolean?,
    val widget_channel_id: Long?,
    val system_channel_id: Long?
)

internal data class UnavailableGuildPacket(val unavailable: Boolean?, val id: Long)

internal data class MemberPacket(
    val user: UserPacket,
    val nick: String?,
    val roles: List<Long>,
    val joined_at: IsoTimestamp,
    val deaf: Boolean,
    val mute: Boolean
)

internal data class PermissionOverwritePacket(
    val id: Long,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)

internal data class RolePacket(
    val id: Long,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
)
