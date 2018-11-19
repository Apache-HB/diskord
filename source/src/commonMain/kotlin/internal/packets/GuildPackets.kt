package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.Color
import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.Member
import com.serebit.diskord.data.toPermissions
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.ChannelCategory
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.cacheAll
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class GuildCreatePacket(
    override val id: Long,
    var name: String,
    var icon: String?,
    var splash: String?,
    @Optional var owner: Boolean = false,
    private var owner_id: Long,
    @Optional private var permissions: BitSet = 0,
    var region: String,
    var afk_channel_id: Long?,
    var afk_timeout: Int,
    @Optional var embed_enabled: Boolean = false,
    @Optional private var embed_channel_id: Long? = null,
    var verification_level: Int,
    var default_message_notifications: Int,
    var explicit_content_filter: Int,
    private var roles: List<RolePacket>,
    var emojis: List<EmotePacket>,
    var features: List<String>,
    var mfa_level: Int,
    var application_id: Long?,
    @Optional var widget_enabled: Boolean = false,
    @Optional private var widget_channel_id: Long? = null,
    private var system_channel_id: Long?,
    private var joined_at: IsoTimestamp,
    var large: Boolean,
    var unavailable: Boolean,
    var member_count: Int,
    var voice_states: List<VoiceStatePacket>,
    private var members: List<MemberPacket>,
    private val channels: MutableList<GenericChannelPacket>,
    var presences: List<PresencePacket>
) : EntityPacket {
    @Transient
    val ownerMember
        get() = memberObjects.find { it.user.id == owner_id }
    @Transient
    val permissionsList
        get() = permissions.toPermissions()
    @Transient
    val typedChannels = channels.map(GenericChannelPacket::toTypedPacket)
    @Transient
    val allChannels
        get() = typedChannels.map { Channel.from(it) }
    @Transient
    val textChannels
        get() = allChannels.filterIsInstance<GuildTextChannel>()
    @Transient
    val voiceChannels
        get() = allChannels.filterIsInstance<GuildVoiceChannel>()
    @Transient
    val channelCategories
        get() = allChannels.filterIsInstance<ChannelCategory>()
    @Transient
    val afkChannel
        get() = voiceChannels.find { it.id == afk_channel_id }
    @Transient
    val embedChannel
        get() = textChannels.find { it.id == embed_channel_id }
    @Transient
    val roleObjects
        get() = roles.map { Role(it.id) }
    @Transient
    val widgetChannel
        get() = textChannels.find { it.id == widget_channel_id }
    @Transient
    val systemChannel
        get() = textChannels.find { it.id == system_channel_id }
    @Transient
    val joinedAt by lazy { DateTime.fromIsoTimestamp(joined_at) }
    @Transient
    val memberObjects by lazy { members.map { Member(it) } }

    init {
        roles.cacheAll()
        typedChannels.cacheAll()
        members.map { it.user }.cacheAll()
    }

    fun update(packet: GuildUpdatePacket): GuildCreatePacket = apply {
        name = packet.name
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
        roles = packet.roles
        emojis = packet.emojis
        features = packet.features
        mfa_level = packet.mfa_level
        application_id = packet.application_id
        widget_enabled = packet.widget_enabled
        widget_channel_id = packet.widget_channel_id
        system_channel_id = packet.system_channel_id
    }
}

/**
 * https://discordapp.com/developers/docs/resources/guild#guild-object
 */
@Serializable
internal data class GuildUpdatePacket(
    override val id: Long,
    val name: String,
    val icon: String?,
    val splash: String?,
    @Optional val owner: Boolean = false,
    val owner_id: Long,
    @Optional val permissions: BitSet = 0,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Int,
    @Optional val embed_enabled: Boolean = false,
    @Optional val embed_channel_id: Long? = null,
    val verification_level: Int,
    val default_message_notifications: Int,
    val explicit_content_filter: Int,
    val roles: List<RolePacket>,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Int,
    val application_id: Long?,
    @Optional val widget_enabled: Boolean = false,
    @Optional val widget_channel_id: Long? = null,
    val system_channel_id: Long?
) : EntityPacket

@Serializable
internal data class UnavailableGuildPacket(
    override val id: Long,
    // if this is unset (which coerces to null), we've been kicked from this guild
    @Optional val unavailable: Boolean? = null
) : EntityPacket

@Serializable
internal data class MemberPacket(
    val user: UserPacket,
    @Optional val nick: String? = null,
    val roles: List<Long>,
    val joined_at: IsoTimestamp,
    val deaf: Boolean,
    val mute: Boolean
)

@Serializable
internal data class PartialMemberPacket(
    @Optional val user: UserPacket? = null,
    @Optional val nick: String? = null,
    @Optional val roles: List<Long> = emptyList(),
    @Optional val joined_at: IsoTimestamp? = null,
    @Optional val deaf: Boolean = false,
    @Optional val mute: Boolean = false
)

@Serializable
internal data class PermissionOverwritePacket(
    val id: Long,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)

@Serializable
internal data class RolePacket(
    override val id: Long,
    val name: String,
    private val color: Int,
    val hoist: Boolean,
    val position: Int,
    private val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
) : EntityPacket {
    @Transient
    val colorObj by lazy { Color(color) }
    @Transient
    val permissionsList by lazy { permissions.toPermissions() }
}
