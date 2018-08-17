package com.serebit.diskord.entities

import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import com.serebit.diskord.entities.channels.GuildChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.MemberPacket

class Guild internal constructor(packet: GuildPacket) : Entity {
    override val id: Snowflake = packet.id
    var textChannels: List<GuildTextChannel>
        private set
    var voiceChannels: List<GuildVoiceChannel>
        private set
    var owner: User = packet.members.map(MemberPacket::user).first { it.id == packet.owner_id }
        private set
    var permissions = Permission.from(packet.permissions ?: 0)
        private set

    init {
        val channels = packet.channels.map(GuildChannel.Companion::from)
        textChannels = channels.filterIsInstance<GuildTextChannel>()
        voiceChannels = channels.filterIsInstance<GuildVoiceChannel>()
        EntityCache.cache(this)
    }
}
