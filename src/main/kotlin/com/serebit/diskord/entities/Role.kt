package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import java.awt.Color

class Role internal constructor(data: Data) : DiscordEntity {
    override val id: Long = data.id
    val name: String = data.name
    val color: Color = Color(data.color)
    val position: Int = data.position
    val permissions = Permission.from(data.permissions)
    val isHoisted = data.hoist
    val isManaged = data.managed
    val isMentionable = data.mentionable

    internal data class Data(
        val id: Snowflake,
        val name: String,
        val color: Int,
        val hoist: Boolean,
        val position: Int,
        val permissions: BitSet,
        val managed: Boolean,
        val mentionable: Boolean
    )
}
