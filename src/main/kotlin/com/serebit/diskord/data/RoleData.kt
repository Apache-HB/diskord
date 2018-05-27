package com.serebit.diskord.data

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake

data class RoleData(
    val id: Snowflake,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
)
