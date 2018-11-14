package com.serebit.diskord.entities

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.Member
import com.serebit.diskord.data.Permission
import com.serebit.diskord.data.toPermissions
import com.serebit.diskord.entities.channels.GuildChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.cacheAll
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.BanGuildMember
import com.serebit.diskord.internal.network.endpoints.GetGuild
import com.serebit.diskord.internal.network.endpoints.KickGuildMember
import com.serebit.diskord.internal.packets.GuildCreatePacket
import io.ktor.http.isSuccess

/**
 * Represents a Discord guild (or "server"), or a self-contained collection of users. Guilds contain their own text
 * and voice channels, and can be customized further with [roles][Role] to segment members into different subgroups.
 */
class Guild internal constructor(packet: GuildCreatePacket) : Entity {
    override val id: Long = packet.id
    val name: String = packet.name
    val joinedAt: DateTime = DateTime.fromIsoTimestamp(packet.joined_at)
    val channels = packet.channels.map(GuildChannel.Companion::from)
    val textChannels: List<GuildTextChannel> = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels: List<GuildVoiceChannel> = channels.filterIsInstance<GuildVoiceChannel>()
    val afkChannel: GuildVoiceChannel? = voiceChannels.find { it.id == packet.afk_channel_id }
    val systemChannel: GuildTextChannel? = textChannels.find { it.id == packet.system_channel_id }
    val afkTimeout: Int = packet.afk_timeout
    val members: List<Member> = packet.members.map(::Member)
    val roles: List<Role> = packet.roles.cacheAll().map { Role(it.id) }
    val owner: User by lazy { members.asSequence().map(Member::user).first { it.id == packet.owner_id } }
    val permissions: List<Permission> by lazy { packet.permissions.toPermissions() }
    val defaultMessageNotifications: Int = packet.default_message_notifications
    val explicitContentFilter: Int = packet.explicit_content_filter
    val enabledFeatures: List<String> = packet.features
    val verificationLevel: Int = packet.verification_level
    val mfaLevel: Int = packet.mfa_level
    val isEmbedEnabled: Boolean = packet.embed_enabled
    val embedChannel: GuildChannel? = channels.find { it.id == packet.embed_channel_id }
    val icon: String? = packet.icon
    val splashImage: String? = packet.splash
    val region: String = packet.region
    val isLarge: Boolean = packet.large

    fun kick(user: User): Boolean = Requester.requestResponse(KickGuildMember(id, user.id)).status.isSuccess()

    fun ban(user: User, deleteMessageDays: Int = 0, reason: String = ""): Boolean =
        Requester.requestResponse(
            BanGuildMember(id, user.id), mapOf(
                "delete-message-days" to deleteMessageDays.toString(),
                "reason" to reason
            )
        ).status.isSuccess()

    companion object {
        internal fun find(id: Long): Guild? = EntityCache.findId(id)
            ?: Requester.requestObject(GetGuild(id))?.let { Guild(it) }
    }
}
