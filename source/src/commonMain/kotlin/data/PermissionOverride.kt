package com.serebit.diskord.data

import com.serebit.diskord.internal.packets.PermissionOverwritePacket

sealed class PermissionOverride {
    abstract val allow: List<Permission>
    abstract val deny: List<Permission>
}

data class RolePermissionOverride(
    val roleId: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride()

data class MemberPermissionOverride(
    val userId: Long,
    override val allow: List<Permission>,
    override val deny: List<Permission>
) : PermissionOverride()

internal fun PermissionOverwritePacket.toOverride() = when (type) {
    "role" -> RolePermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    "member" -> MemberPermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    else -> null
}

internal fun Iterable<PermissionOverwritePacket>.toOverrides() = mapNotNull { it.toOverride() }
