package com.serebit.strife.entities

import com.serebit.strife.internal.entitydata.RoleData

/**
 * Represents a role in a Discord server. Roles are used to group users,
 * and those groups can be given specific name colors and permissions.
 */
class Role internal constructor(private val data: RoleData) : Entity {
    override val id = data.id
    override val context = data.context
    /** The name of this role. */
    val name get() = data.name
    /** The position of this role in its parent guild's role hierarchy. */
    val position get() = data.position
    /** The color assigned to this role as a Java color. */
    val color get() = data.color
    /** The permissions assigned to this role. */
    val permissions get() = data.permissions
    /** Whether or not this role appears as its own section in the sidebar. */
    val isHoisted get() = data.isHoisted
    /** Whether or not this role is managed by an external source (e.g. Patreon or a Discord bot). */
    val isManaged get() = data.isManaged
    /** Whether or not this role can be mentioned in chat. */
    val isMentionable get() = data.isMentionable

    override fun equals(other: Any?) = other is Role && other.id == id
}
