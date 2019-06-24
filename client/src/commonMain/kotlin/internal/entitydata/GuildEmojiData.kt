package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.GuildEmoji
import com.serebit.strife.internal.packets.GuildEmojiPacket

internal class GuildEmojiData(
    packet: GuildEmojiPacket,
    override val context: BotClient
) : EntityData<GuildEmojiPacket, GuildEmoji> {
    override val id = packet.id
    var name = packet.name
    var roles = packet.roles.toSet()
    val creator = packet.user?.toData(context)
    var isManaged = packet.managed
    var requiresColons = packet.require_colons
    val isAnimated = packet.animated

    override fun update(packet: GuildEmojiPacket) {
        name = packet.name
        roles = packet.roles.toSet()
        isManaged = packet.managed
        requiresColons = packet.require_colons
    }

    override val lazyEntity by lazy { GuildEmoji(this) }
}

internal fun GuildEmojiPacket.toData(context: BotClient) = GuildEmojiData(this, context)
