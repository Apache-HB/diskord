package com.serebit.strife.data

import com.serebit.strife.internal.packets.PermissionOverwritePacket

sealed class PermissionOverride {
    abstract val allow: Set<Permission>
    abstract val deny: Set<Permission>
}

data class RolePermissionOverride(
    val roleId: Long,
    override val allow: Set<Permission>,
    override val deny: Set<Permission>
) : PermissionOverride()

data class MemberPermissionOverride(
    val userId: Long,
    override val allow: Set<Permission>,
    override val deny: Set<Permission>
) : PermissionOverride()

internal fun PermissionOverwritePacket.toOverride() = when (type) {
    "role" -> RolePermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    "member" -> MemberPermissionOverride(id, allow.toPermissions(), deny.toPermissions())
    else -> null
}

internal fun Iterable<PermissionOverwritePacket>.toOverrides() = mapNotNull { it.toOverride() }
