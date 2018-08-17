package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import com.serebit.diskord.internal.EntityCache
import java.awt.Color

/**
 * Represents a role in a Discord server.
 */
class Role internal constructor(
    override val id: Snowflake,
    val name: String,
    color: Int,
    hoist: Boolean,
    val position: Int,
    permissions: BitSet,
    managed: Boolean,
    mentionable: Boolean
) : Entity {
    /**
     * The color assigned to this role as a Java color.
     */
    val color = Color(color)
    /**
     * The permissions assigned to this role.
     */
    val permissions = Permission.from(permissions)
    /**
     * Whether or not this role appears as its own section in the sidebar.
     */
    val isHoisted = hoist
    /**
     * Whether or not this role is managed by an external source, such as Patreon or a Discord bot.
     */
    val isManaged = managed
    /**
     * Whether or not this role can be mentioned in chat.
     */
    val isMentionable = mentionable

    init {
        EntityCache.cache(this)
    }
}
