package com.serebit.diskord.entities

import com.serebit.diskord.data.Permission
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.RolePacket
import java.awt.Color

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
    var name = packet.name
        private set
    /**
     * The position of this role in its parent guild's role hierarchy.
     */
    var position = packet.position
        private set
    /**
     * The color assigned to this role as a Java color.
     */
    var color = Color(packet.color)
        private set
    /**
     * The permissions assigned to this role.
     */
    var permissions = Permission.from(packet.permissions)
        private set
    /**
     * Whether or not this role appears as its own section in the sidebar.
     */
    var isHoisted = packet.hoist
        private set
    /**
     * Whether or not this role is managed by an external source, such as Patreon or a Discord bot.
     */
    var isManaged = packet.managed
        private set
    /**
     * Whether or not this role can be mentioned in chat.
     */
    var isMentionable = packet.mentionable
        private set

    init {
        EntityCache.cache(this)
    }
}
