package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.data.toPermissions
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildVoiceChannelData
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket

internal class GuildData(packet: GuildCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    var name = packet.name
    var iconHash = packet.icon
    var splashHash = packet.splash
    var isOwner = packet.owner
    var owner = context.cache.users[packet.owner_id]!!
    var permissions = packet.permissions.toPermissions()
    var region = packet.region
    var afkChannel = packet.afk_channel_id?.let { context.cache.findChannel<GuildVoiceChannelData>(it) }
    var afkTimeout = packet.afk_timeout
    var isEmbedEnabled = packet.embed_enabled
    var embedChannel = packet.embed_channel_id?.let { context.cache.findChannel<GuildChannelData>(it) }
    var verificationLevel = packet.verification_level
    var defaultMessageNotifications = packet.default_message_notifications
    var explicitContentFilter = packet.explicit_content_filter
    val roles = packet.roles.map { RoleData(it, context) }.toMutableList()
    var emojis = packet.emojis
    var features = packet.features
    var mfaLevel = packet.mfa_level
    var applicationId = packet.application_id
    var isWidgetEnabled = packet.widget_enabled
    var widgetChannel = packet.widget_channel_id?.let { context.cache.findChannel<GuildChannelData>(it) }
    var systemChannel = packet.system_channel_id?.let { context.cache.findChannel<GuildTextChannelData>(it) }
    val joinedAt = packet.joined_at
    val isLarge = packet.large
    val isUnavailable = packet.unavailable
    var memberCount = packet.member_count
    val voiceStates = packet.voice_states.toMutableList()
    val members = packet.members.toMutableList()
    val allChannels = packet.channels.map { it.toTypedPacket().toData(context) }.toMutableList()
    val presences = packet.presences.toMutableList()

    fun update(packet: GuildUpdatePacket) = apply {
        name = packet.name
        iconHash = packet.icon
        splashHash = packet.splash
        isOwner = packet.owner
        owner = context.cache.users[packet.owner_id]!!
        permissions = packet.permissions.toPermissions()
        region = packet.region
        afkChannel = packet.afk_channel_id?.let { context.cache.findChannel(it) }
        afkTimeout = packet.afk_timeout
        isEmbedEnabled = packet.embed_enabled
        embedChannel = packet.embed_channel_id?.let { context.cache.findChannel(it) }
        verificationLevel = packet.verification_level
        defaultMessageNotifications = packet.default_message_notifications
        explicitContentFilter = packet.explicit_content_filter
        emojis = packet.emojis
        features = packet.features
        mfaLevel = packet.mfa_level
        applicationId = packet.application_id
        isWidgetEnabled = packet.widget_enabled
        widgetChannel = packet.widget_channel_id?.let { context.cache.findChannel(it) }
        systemChannel = packet.system_channel_id?.let { context.cache.findChannel(it) }
    }
}
