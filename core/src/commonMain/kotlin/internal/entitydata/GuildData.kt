package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.data.Member
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.ExplicitContentFilterLevel
import com.serebit.strife.entities.MessageNotificationLevel
import com.serebit.strife.entities.MfaLevel
import com.serebit.strife.entities.VerificationLevel
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.entitydata.channels.GuildTextChannelData
import com.serebit.strife.internal.entitydata.channels.GuildVoiceChannelData
import com.serebit.strife.internal.entitydata.channels.toGuildChannelData
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

internal class GuildData(packet: GuildCreatePacket, override val context: Context) : EntityData {
    override val id = packet.id
    var name = packet.name
    var iconHash = packet.icon
    var splashHash = packet.splash
    var isOwner = packet.owner
    var permissions = packet.permissions.toPermissions()
    var region = packet.region
    val allChannels = packet.channels
        .map { it.toTypedPacket().toGuildChannelData(this, context) }
        .associateBy { it.id }
        .toMutableMap()
    var afkChannel = packet.afk_channel_id?.let { allChannels[it] as GuildVoiceChannelData }
    var afkTimeout = packet.afk_timeout
    var isEmbedEnabled = packet.embed_enabled
    var embedChannel = packet.embed_channel_id?.let { allChannels[it] }
    var verificationLevel = VerificationLevel.values()[packet.verification_level]
    var defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications]
    var explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter]
    val roles = packet.roles.map { it.toData(context) }.associateBy { it.id }.toMutableMap()
    var emojis = packet.emojis
    var features = packet.features
    var mfaLevel = MfaLevel.values()[packet.mfa_level]
    var applicationId = packet.application_id
    var isWidgetEnabled = packet.widget_enabled
    var widgetChannel = packet.widget_channel_id?.let { allChannels[it] }
    var systemChannel = packet.system_channel_id?.let { allChannels[it] as? GuildTextChannelData }
    val joinedAt = DateFormat.ISO_FORMAT.parse(packet.joined_at)
    val isLarge = packet.large
    val isUnavailable = packet.unavailable
    var memberCount = packet.member_count
    val voiceStates = packet.voice_states.toMutableList()
    val members = packet.members.map { Member(it, this, context) }.toMutableList()
    var owner = members.first { it.user.id == context.userCache[packet.owner_id]!!.id }
    val presences = packet.presences.toMutableList()

    fun update(packet: GuildUpdatePacket) = apply {
        name = packet.name
        iconHash = packet.icon
        splashHash = packet.splash
        isOwner = packet.owner
        owner = members.find { it.user.id == context.userCache[packet.owner_id]!!.id }!!
        permissions = packet.permissions.toPermissions()
        region = packet.region
        afkChannel = packet.afk_channel_id?.let { allChannels[it] as GuildVoiceChannelData }
        afkTimeout = packet.afk_timeout
        isEmbedEnabled = packet.embed_enabled
        embedChannel = packet.embed_channel_id?.let { allChannels[it] }
        verificationLevel = VerificationLevel.values()[packet.verification_level]
        defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications]
        explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter]
        emojis = packet.emojis
        features = packet.features
        mfaLevel = MfaLevel.values()[packet.mfa_level]
        applicationId = packet.application_id
        isWidgetEnabled = packet.widget_enabled
        widgetChannel = packet.embed_channel_id?.let { allChannels[it] }
        systemChannel = packet.embed_channel_id?.let { allChannels[it] as GuildTextChannelData }
    }
}

internal fun GuildCreatePacket.toData(context: Context) = GuildData(this, context)
