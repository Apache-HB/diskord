package com.serebit.diskord.data

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

data class RolePermissionOverride(
    val roleId: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride

data class MemberPermissionOverride(
    val userId: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride
