package com.serebit.diskord.entities

import com.serebit.diskord.data.Color
import com.serebit.diskord.data.toPermissions
import com.serebit.diskord.internal.packets.RolePacket

/**
 * Represents a role in a Discord server.
 */
class Role internal constructor(packet: RolePacket) : Entity {
    /**
     * The role's unique ID.
     */
    override val id = packet.id
    /**
     * The name of this role.
     */
    val name = packet.name
    /**
     * The position of this role in its parent guild's role hierarchy.
     */
    val position = packet.position
    /**
     * The color assigned to this role as a Java color.
     */
    val color = Color(packet.color)
    /**
     * The permissions assigned to this role.
     */
    val permissions = packet.permissions.toPermissions()
    /**
     * Whether or not this role appears as its own section in the sidebar.
     */
    val isHoisted = packet.hoist
    /**
     * Whether or not this role is managed by an external source, such as Patreon or a Discord bot.
     */
    val isManaged = packet.managed
    /**
     * Whether or not this role can be mentioned in chat.
     */
    val isMentionable = packet.mentionable
}
