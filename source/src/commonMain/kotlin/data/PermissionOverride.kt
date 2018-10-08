package com.serebit.diskord.data

import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Role
import com.serebit.diskord.internal.EntityCache
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
    val role by lazy { EntityCache.findId<Role>(id)!! }
}

class MemberPermissionOverride(
    id: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride {
    val member by lazy {
        EntityCache.filterIsInstance<Guild>()
            .map { it.members }
            .flatten()
            .first { it.user.id == id }
    }
}
