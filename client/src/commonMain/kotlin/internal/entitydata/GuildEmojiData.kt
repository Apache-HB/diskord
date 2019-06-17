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
    var name = packet.name
    var roles = packet.roles.mapNotNull { guild.roles[it] }
    val creator = packet.user?.toData(context)
    var isManaged = packet.managed
    var requiresColons = packet.require_colons
    val isAnimated = packet.animated

    override fun update(packet: GuildEmojiPacket) {
        name = packet.name
        roles = packet.roles.mapNotNull { guild.roles[it] }
        isManaged = packet.managed
        requiresColons = packet.require_colons
    }

    override val lazyEntity by lazy { GuildEmoji(this) }
}

internal fun GuildEmojiPacket.toData(guild: GuildData, context: BotClient) = GuildEmojiData(this, guild, context)
