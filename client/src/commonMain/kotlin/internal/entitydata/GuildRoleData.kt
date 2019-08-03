package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.toColor
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.GuildRole
import com.serebit.strife.internal.packets.GuildRolePacket

internal class GuildRoleData(packet: GuildRolePacket, override val context: BotClient) :
    EntityData<GuildRolePacket, GuildRole> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildRole(this) }
    var name = packet.name
        private set
    var color = packet.color.toColor()
        private set
    var isHoisted = packet.hoist
        private set
    var position = packet.position
        private set
    var permissions = packet.permissions.toPermissions()
        private set
    var isManaged = packet.managed
        private set
    var isMentionable = packet.mentionable
        private set

    override fun update(packet: GuildRolePacket) {
        name = packet.name
        color = packet.color.toColor()
        isHoisted = packet.hoist
        position = packet.position
        permissions = packet.permissions.toPermissions()
        isManaged = packet.managed
        isMentionable = packet.mentionable
    }
}

internal fun GuildRolePacket.toData(context: BotClient) = GuildRoleData(this, context)
