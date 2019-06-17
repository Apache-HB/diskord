package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.Activity
import com.serebit.strife.data.toActivity
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.*
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.*
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse

internal class GuildData(
    packet: GuildCreatePacket, override val context: BotClient
) : EntityData<GuildUpdatePacket, Guild> {
    override val id = packet.id
    override val lazyEntity by lazy { Guild(this) }
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
    var verificationLevel = VerificationLevel.values()[packet.verification_level.toInt()]
    var defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications.toInt()]
    var explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter.toInt()]
    val roles = packet.roles
        .map { it.toData(context) }
        .associateBy { it.id }
        .toMutableMap()
    var emojis = packet.emojis
    var features = packet.features
    var mfaLevel = MfaLevel.values()[packet.mfa_level.toInt()]
    var applicationID = packet.application_id
    var isWidgetEnabled = packet.widget_enabled
    var widgetChannel = packet.widget_channel_id?.let { allChannels[it] }
    var systemChannel = packet.system_channel_id?.let { allChannels[it] as? GuildTextChannelData }
    val joinedAt = packet.joined_at.let { DateFormat.ISO_WITH_MS.parse(it) }
    val isLarge = packet.large
    val isUnavailable = packet.unavailable
    var memberCount = packet.member_count
    val voiceStates = packet.voice_states.toMutableList()
    val members = packet.members
        .map { it.toData(this, context) }
        .associateBy { it.user.id }
        .toMutableMap()
    var owner = members.getValue(packet.owner_id)
    val presences = packet.presences.toMutableList()

    override fun update(packet: GuildUpdatePacket) {
        name = packet.name
        iconHash = packet.icon
        splashHash = packet.splash
        isOwner = packet.owner
        owner = members.getValue(packet.owner_id)
        permissions = packet.permissions.toPermissions()
        region = packet.region
        afkChannel = packet.afk_channel_id?.let { allChannels[it] as GuildVoiceChannelData }
        afkTimeout = packet.afk_timeout
        isEmbedEnabled = packet.embed_enabled
        embedChannel = packet.embed_channel_id?.let { allChannels[it] }
        verificationLevel = VerificationLevel.values()[packet.verification_level.toInt()]
        defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications.toInt()]
        explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter.toInt()]
        emojis = packet.emojis
        features = packet.features
        mfaLevel = MfaLevel.values()[packet.mfa_level.toInt()]
        applicationID = packet.application_id
        isWidgetEnabled = packet.widget_enabled
        widgetChannel = packet.embed_channel_id?.let { allChannels[it] }
        systemChannel = packet.embed_channel_id?.let { allChannels[it] as GuildTextChannelData }
    }
}

internal fun GuildCreatePacket.toData(context: BotClient) = GuildData(this, context)


internal class GuildMemberData(packet: GuildMemberPacket, val guild: GuildData, val context: BotClient) {
    val user: UserData = context.cache.pullUserData(packet.user)
    var roles: List<GuildRoleData> = packet.roles.mapNotNull { guild.roles[it] }
    var nickname: String? = packet.nick
    var activity: Activity? = null
    val joinedAt: DateTimeTz = try {
        DateFormat.ISO_WITH_MS.parse(packet.joined_at)
    } catch (ex: Exception) {
        DateFormat.ISO_WITHOUT_MS.parse(packet.joined_at)
    }
    var isDeafened: Boolean = packet.deaf
    var isMuted: Boolean = packet.mute

    fun update(roleIDs: List<Long>, nick: String?) {
        nickname = nick
        update(roleIDs)
    }

    fun update(packet: PresencePacket) {
        packet.apply {
            update(roles)
            activity = game?.toActivity()
        }
    }

    fun update(roleIDs: List<Long>) {
        roles = roleIDs.mapNotNull { guild.roles[it] }
    }

    fun toMember() = GuildMember(this)
}

internal fun GuildMemberPacket.toData(guild: GuildData, context: BotClient) = GuildMemberData(this, guild, context)