package com.serebit.diskord.entities

import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import com.serebit.diskord.entities.channels.GuildChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetGuild
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.MemberPacket

class Guild internal constructor(packet: GuildPacket) : Entity {
    override val id: Snowflake = packet.id
    private val channels by lazy { packet.channels.map { GuildChannel.from(this, it) } }
    val textChannels: List<GuildTextChannel> get() = channels.filterIsInstance<GuildTextChannel>()
    val voiceChannels: List<GuildVoiceChannel> get() = channels.filterIsInstance<GuildVoiceChannel>()
    val owner: User = packet.members.asSequence().map(MemberPacket::user).first { it.id == packet.owner_id }
    val permissions = Permission.from(packet.permissions ?: 0)
    val icon: String? = packet.icon
    val splashImage: String? = packet.splash
    val region: String = packet.region
    val isLarge: Boolean = packet.large ?: false

    companion object {
        internal fun find(id: Long): Guild? = EntityCache.find(id)
            ?: Requester.requestObject(GetGuild(id))?.let { Guild(it) }
    }
}
