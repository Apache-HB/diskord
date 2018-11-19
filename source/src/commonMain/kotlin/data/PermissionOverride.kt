package com.serebit.diskord.data

import com.serebit.diskord.entities.Role
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.PermissionOverwritePacket

interface PermissionOverride {
    val allow: List<Permission>
    val deny: List<Permission>

    companion object {
        internal fun from(packet: PermissionOverwritePacket): PermissionOverride? = when (packet.type) {
            "role" -> RolePermissionOverride(packet.id, packet.allow.toPermissions(), packet.deny.toPermissions())
            "member" -> MemberPermissionOverride(packet.id, packet.allow.toPermissions(), packet.deny.toPermissions())
            else -> null
        }
    }
}

class RolePermissionOverride(
    id: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride {
    val role by lazy { Role(id) }
}

class MemberPermissionOverride(
    id: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride {
    val member by lazy {
        EntityPacketCache.filterIsInstance<GuildCreatePacket>()
            .map { it.memberObjects }
            .flatten()
            .first { it.user.id == id }
    }
}
