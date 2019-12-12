package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.GuildEmoji
import com.serebit.strife.internal.packets.GuildEmojiPacket

internal class GuildEmojiData(
    packet: GuildEmojiPacket,
    val guild: GuildData,
    override val context: BotClient
) : EntityData<GuildEmojiPacket, GuildEmoji> {
    override val id = packet.id
    val creator = packet.user?.toData(context)
    val isAnimated = packet.animated
    var name = packet.name
        private set
    var roles = packet.roles.mapNotNull { guild.getRoleData(it) }
        private set
    var isManaged = packet.managed
        private set
    var requiresColons = packet.require_colons
        private set

    override fun update(packet: GuildEmojiPacket) {
        name = packet.name
        roles = packet.roles.mapNotNull { guild.getRoleData(it) }
        isManaged = packet.managed
        requiresColons = packet.require_colons
    }

    override val lazyEntity by lazy { GuildEmoji(id, guild.id, context) }
}

internal fun GuildEmojiPacket.toData(guild: GuildData, context: BotClient) =
    GuildEmojiData(this, guild, context)
