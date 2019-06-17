package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.toColor
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.GuildRole
import com.serebit.strife.internal.packets.RolePacket

internal class GuildRoleData(packet: RolePacket, override val context: BotClient) : EntityData<RolePacket, GuildRole> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildRole(this) }
    var name = packet.name
    var color = packet.color.toColor()
    var isHoisted = packet.hoist
    var position = packet.position
    var permissions = packet.permissions.toPermissions()
    var isManaged = packet.managed
    var isMentionable = packet.mentionable

    override fun update(packet: RolePacket) {
        name = packet.name
        color = packet.color.toColor()
        isHoisted = packet.hoist
        position = packet.position
        permissions = packet.permissions.toPermissions()
        isManaged = packet.managed
        isMentionable = packet.mentionable
    }
}

internal fun RolePacket.toData(context: BotClient) = GuildRoleData(this, context)
