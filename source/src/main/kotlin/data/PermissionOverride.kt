package com.serebit.diskord.data

import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Role
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.PermissionOverwritePacket

interface PermissionOverride {
    val allow: List<Permission>
    val deny: List<Permission>

    companion object {
        internal fun from(packet: PermissionOverwritePacket): PermissionOverride? = when {
            packet.type == "role" -> RolePermissionOverride(
                EntityCache.find(packet.id)!!, Permission.from(packet.allow), Permission.from(packet.deny)
            )
            packet.type == "member" -> {
                val member = EntityCache.filterIsInstance<Guild>()
                    .map { it.members }
                    .flatten()
                    .first { it.user.id == packet.id }
                MemberPermissionOverride(member, Permission.from(packet.allow), Permission.from(packet.deny))
            }
            else -> null
        }
    }
}

class RolePermissionOverride(
    val role: Role,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride

class MemberPermissionOverride(
    val member: Member,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride
