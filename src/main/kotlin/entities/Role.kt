package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import java.awt.Color

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
    val color: Color = Color(color)
    val permissions = Permission.from(permissions)
    val isHoisted = hoist
    val isManaged = managed
    val isMentionable = mentionable
}
