package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context
import com.serebit.diskord.internal.packets.RolePacket

internal class RoleData(packet: RolePacket, override val context: Context) : EntityData {
    override val id = packet.id
    var name = packet.name
    var colorAsHex = packet.color
    var isHoisted = packet.hoist
    var position = packet.position
    var permissionsBitSet = packet.permissions
    var isManaged = packet.managed
    var isMentionable = packet.mentionable

    fun update(packet: RolePacket) = apply {
        name = packet.name
        colorAsHex = packet.color
        isHoisted = packet.hoist
        position = packet.position
        permissionsBitSet = packet.permissions
        isManaged = packet.managed
        isMentionable = packet.mentionable
    }
}
