package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.data.toColor
import com.serebit.diskord.data.toPermissions
import com.serebit.diskord.internal.packets.RolePacket

internal class RoleData(packet: RolePacket, override val context: Context) : EntityData {
    override val id = packet.id
    var name = packet.name
    var color = packet.color.toColor()
    var isHoisted = packet.hoist
    var position = packet.position
    var permissions = packet.permissions.toPermissions()
    var isManaged = packet.managed
    var isMentionable = packet.mentionable

    fun update(packet: RolePacket) = apply {
        name = packet.name
        color = packet.color.toColor()
        isHoisted = packet.hoist
        position = packet.position
        permissions = packet.permissions.toPermissions()
        isManaged = packet.managed
        isMentionable = packet.mentionable
    }
}

internal fun RolePacket.toData(context: Context) = RoleData(this, context)
