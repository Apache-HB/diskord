package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket

internal class GuildData(packet: GuildCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    var name = packet.name
    var iconHash = packet.icon
    var splashHash = packet.splash
    var isOwner = packet.owner
    var ownerId = packet.owner_id
    var permissionsBitSet = packet.permissions
    var region = packet.region
    var afkChannelId = packet.afk_channel_id
    var afkTimeout = packet.afk_timeout
    var embedEnabled = packet.embed_enabled
    var embedChannelId = packet.embed_channel_id
    var verificationLevel = packet.verification_level
    var defaultMessageNotifications = packet.default_message_notifications
    var explicitContentFilter = packet.explicit_content_filter
    var roles = packet.roles
    var emojis = packet.emojis
    var features = packet.features
    var mfaLevel = packet.mfa_level
    var applicationId = packet.application_id
    var isWidgetEnabled = packet.widget_enabled
    var widgetChannelId = packet.widget_channel_id
    var systemChannelId = packet.system_channel_id
    val joinedAt = packet.joined_at
    val isLarge = packet.large
    val isUnavailable = packet.unavailable
    var memberCount = packet.member_count
    val voiceStates = packet.voice_states.toMutableList()
    val members = packet.members.toMutableList()
    val channels = packet.channels.map { it.toTypedPacket().toData(context) }.toMutableList()
    val presences = packet.presences.toMutableList()

    fun update(packet: GuildUpdatePacket) = apply {
        name = packet.name
        iconHash = packet.icon
        splashHash = packet.splash
        isOwner = packet.owner
        ownerId = packet.owner_id
        permissionsBitSet = packet.permissions
        region = packet.region
        afkChannelId = packet.afk_channel_id
        afkTimeout = packet.afk_timeout
        embedEnabled = packet.embed_enabled
        embedChannelId = packet.embed_channel_id
        verificationLevel = packet.verification_level
        defaultMessageNotifications = packet.default_message_notifications
        explicitContentFilter = packet.explicit_content_filter
        roles = packet.roles
        emojis = packet.emojis
        features = packet.features
        mfaLevel = packet.mfa_level
        applicationId = packet.application_id
        isWidgetEnabled = packet.widget_enabled
        widgetChannelId = packet.widget_channel_id
        systemChannelId = packet.system_channel_id
    }
}
