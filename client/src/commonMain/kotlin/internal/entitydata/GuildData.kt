package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.*
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.set
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse

internal class GuildData(
    packet: GuildCreatePacket, override val context: BotClient
) : EntityData<GuildUpdatePacket, Guild> {
    override val id = packet.id
    override val lazyEntity by lazy { Guild(this) }
    val joinedAt = packet.joined_at?.let { DateFormat.ISO_WITH_MS.parse(it) }
    val isLarge = packet.large

    private val channels = packet.channels.asSequence()
        .map { context.cache.pullGuildChannelData(this, it.toTypedPacket()) }
        .associateBy { it.id }
        .toMutableMap()

    val channelList get() = channels.values

    private val roles = packet.roles.asSequence()
        .map { context.cache.pullRoleData(it) }
        .associateBy { it.id }
        .toMutableMap()

    val roleList get() = roles.values

    private var emojis = packet.emojis.asSequence()
        .map { context.cache.pullEmojiData(it) }
        .associateBy { it.id }
        .toMap()

    private val emojiList get() = emojis.values

    private val members = LruWeakCache<Long, GuildMemberData>().also {
        packet.members.forEach { member ->
            val userData = context.cache.pullUserData(member.user)
            it[userData.id] = member.toData(this, context)
        }
    }

    val memberList get() = members.values

    // TODO: Integrate voice state data
    val voiceStates = packet.voice_states.toMutableList()
    // TODO: Integrate presence data
    val presences = packet.presences.toMutableList()

    var name = packet.name
        private set
    var iconHash = packet.icon
        private set
    var splashHash = packet.splash
        private set
    var isOwner = packet.owner
        private set
    var permissions = packet.permissions.toPermissions()
        private set
    var region = packet.region
        private set
    var afkChannel = packet.afk_channel_id?.let { channels[it] as GuildVoiceChannelData }
        private set
    var afkTimeout = packet.afk_timeout
        private set
    var isEmbedEnabled = packet.embed_enabled
        private set
    var embedChannel = packet.embed_channel_id?.let { channels[it] }
        private set
    var verificationLevel = VerificationLevel.values()[packet.verification_level.toInt()]
        private set
    var defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications.toInt()]
        private set
    var explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter.toInt()]
        private set
    var features = packet.features
        private set
    var mfaLevel = MfaLevel.values()[packet.mfa_level.toInt()]
        private set
    var applicationID = packet.application_id
        private set
    var isWidgetEnabled = packet.widget_enabled
        private set
    var widgetChannel = packet.widget_channel_id?.let { channels[it]!! }
        private set
    var systemChannel = packet.system_channel_id?.let { channels[it]!! as? GuildTextChannelData }
        private set
    var memberCount = packet.member_count
        private set

    override fun update(packet: GuildUpdatePacket) {
        name = packet.name
        iconHash = packet.icon
        splashHash = packet.splash
        isOwner = packet.owner
        permissions = packet.permissions.toPermissions()
        region = packet.region
        afkChannel = packet.afk_channel_id?.let { channels[it] as GuildVoiceChannelData }
        afkTimeout = packet.afk_timeout
        isEmbedEnabled = packet.embed_enabled
        embedChannel = packet.embed_channel_id?.let { channels[it] }
        verificationLevel = VerificationLevel.values()[packet.verification_level.toInt()]
        defaultMessageNotifications = MessageNotificationLevel.values()[packet.default_message_notifications.toInt()]
        explicitContentFilter = ExplicitContentFilterLevel.values()[packet.explicit_content_filter.toInt()]
        features = packet.features
        mfaLevel = MfaLevel.values()[packet.mfa_level.toInt()]
        applicationID = packet.application_id
        isWidgetEnabled = packet.widget_enabled
        widgetChannel = packet.embed_channel_id?.let { channels[it] }
        systemChannel = packet.embed_channel_id?.let { channels[it] as GuildTextChannelData }
        emojis = packet.emojis.asSequence()
            .map { context.cache.pullEmojiData(it) }
            .associateBy { it.id }
            .toMap()
    }

    fun getMemberData(id: Long) = members[id]

    fun getChannelData(id: Long) = channels[id]

    fun getRoleData(id: Long) = roles[id]

    fun getEmojiData(id: Long) = emojis[id]
}

internal fun GuildCreatePacket.toData(context: BotClient) = GuildData(this, context)

internal class GuildMemberData(packet: GuildMemberPacket, val guild: GuildData, val context: BotClient) {
    val lazyMember by lazy { GuildMember(this) }
    val user: UserData = context.cache.pullUserData(packet.user)
    var roles: List<GuildRoleData> = packet.roles.map { guild.getRoleData(it)!! }
    var nickname: String? = packet.nick
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
        }
    }

    fun update(roleIDs: List<Long>) {
        roles = roleIDs.map { guild.getRoleData(it)!! }
    }
}

internal fun GuildMemberPacket.toData(guild: GuildData, context: BotClient) =
    GuildMemberData(this, guild, context)
