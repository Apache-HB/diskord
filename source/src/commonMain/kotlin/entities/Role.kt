package com.serebit.diskord.entities

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.packets.RolePacket

/**
 * Represents a role in a Discord server. Roles are used to group users, and those groups can be given specific name
 * colors and permissions.
 */
class Role internal constructor(override val id: Long) : Entity {
    private val packet: RolePacket
        get() = EntityPacketCache.findId(id)
            ?: throw EntityNotFoundException("Invalid message instantiated with ID $id.")
    /**
     * The name of this role.
     */
    val name get() = packet.name
    /**
     * The position of this role in its parent guild's role hierarchy.
     */
    val position get() = packet.position
    /**
     * The color assigned to this role as a Java color.
     */
    val color get() = packet.colorObj
    /**
     * The permissions assigned to this role.
     */
    val permissions get() = packet.permissionsList
    /**
     * Whether or not this role appears as its own section in the sidebar.
     */
    val isHoisted get() = packet.hoist
    /**
     * Whether or not this role is managed by an external source, such as Patreon or a Discord bot.
     */
    val isManaged get() = packet.managed
    /**
     * Whether or not this role can be mentioned in chat.
     */
    val isMentionable get() = packet.mentionable
}
