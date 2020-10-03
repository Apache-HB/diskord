package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.data.Presence
import com.serebit.strife.data.toPermissions
import com.serebit.strife.data.toPresence
import com.serebit.strife.data.toVoiceState
import com.serebit.strife.entities.*
import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.dispatches.GuildEmojisUpdate
import com.serebit.strife.internal.dispatches.GuildMemberRemove
import com.serebit.strife.internal.dispatches.GuildMemberUpdate
import com.serebit.strife.internal.dispatches.GuildRoleDelete
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.parseSafe
import com.serebit.strife.internal.set
import kotlinx.datetime.Instant

internal class GuildData(
    packet: GuildCreatePacket, override val context: BotClient
) : EntityData<GuildUpdatePacket, Guild> {
    override val id = packet.id
    override val lazyEntity by lazy { Guild(this) }
    val joinedAt = packet.joined_at?.let { Instant.parseSafe(it) }
    val isLarge = packet.large

    private val channels = packet.channels.asSequence()
        .map { context.cache.pullGuildChannelData(this, it) }
        .associateBy { it.id }
        .toMutableMap()

    val channelList: Collection<GuildChannelData<*, *>> get() = channels.values

    val roles = packet.roles.asSequence()
        .map { context.cache.pullRoleData(it) }
        .onEach { it.guildID = id }
        .associateBy { it.id }
        .toMutableMap()

    private var emojis = packet.emojis.asSequence()
        .map { context.cache.pullEmojiData(this, it) }
        .associateBy { it.id }
        .toMap()

    val emojiList get() = emojis.values

    private val members = LruWeakCache<Long, GuildMemberData>().also {
        packet.members.forEach { member -> it[member.user.id] = member.toData(this, context) }
    }

    val memberList: Collection<GuildMemberData> get() = members.values

    private val presences = packet.presences.asSequence()
        .map { it.toPresence(lazyEntity, context) }
        .associateBy { it.userID }
        .toMutableMap()

    private val voiceStates = packet.voice_states.asSequence()
        .map { it.toVoiceState(lazyEntity, context) }
        .associateBy { it.userID }
        .toMutableMap()

    val presenceList: Collection<Presence> get() = presences.values

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
    var ownerID = packet.owner_id
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
        ownerID = packet.owner_id
        emojis = packet.emojis.asSequence()
            .map { context.cache.pullEmojiData(this, it) }
            .associateBy { it.id }
            .toMap()
    }

    fun update(packet: GuildChannelPacket) = context.cache.pullGuildChannelData(this, packet)
        .also { channels[it.id] = it }

    fun update(packet: GuildRolePacket) = context.cache.pullRoleData(packet)
        .also { roles[it.id] = it }

    fun update(data: GuildRoleDelete.Data) {
        roles.remove(data.role_id)
        context.cache.remove(RemoveCacheData.GuildRole(data.role_id))
    }

    fun update(data: GuildEmojisUpdate.Data) {
        emojis = data.emojis.asSequence()
            .map { context.cache.pullEmojiData(this, it) }
            .associateBy { it.id }
            .toMap()
    }

    fun update(packet: GuildMemberPacket) = packet.toData(this, context)
        .also { members[packet.user.id] = it }

    fun update(data: GuildMemberRemove.Data) {
        members.remove(data.user.id)
    }

    fun update(packet: PresencePacket) = packet
        .also { members[it.user.id]?.update(it) }
        .toPresence(lazyEntity, context)
        .also { presences[it.userID] = it }

    fun update(packet: VoiceStatePacket) = packet.toVoiceState(lazyEntity, context).also { voiceStates[it.userID] = it }

    fun getChannelData(id: Long) = channels[id]

    fun getRoleData(id: Long) = roles[id]

    fun getEmojiData(id: Long) = emojis[id]

    fun getMemberData(id: Long) = members[id]

    fun getPresence(id: Long) = presences[id]

    fun getVoiceState(id: Long) = voiceStates[id]
}

internal fun GuildCreatePacket.toData(context: BotClient) = GuildData(this, context)

internal class GuildMemberData(packet: GuildMemberPacket, val guild: GuildData, val context: BotClient) {
    val lazyMember by lazy { GuildMember(this) }
    val user: UserData = context.cache.pullUserData(packet.user)
    var roles: List<GuildRoleData> = packet.roles.map { guild.getRoleData(it)!! }
    var nickname: String? = packet.nick
    val joinedAt: Instant = Instant.parseSafe(packet.joined_at)
    var isDeafened: Boolean = packet.deaf
    var isMuted: Boolean = packet.mute

    fun update(data: GuildMemberUpdate.Data) {
        context.cache.pullUserData(data.user)
        nickname = data.nick
        update(data.roles)
    }

    fun update(packet: PresencePacket) {
        packet.apply {
            update(roles)
        }
    }

    private fun update(roleIDs: List<Long>) {
        roles = roleIDs.map { guild.getRoleData(it)!! }
    }
}

private fun GuildMemberPacket.toData(guild: GuildData, context: BotClient) =
    GuildMemberData(this, guild, context)
