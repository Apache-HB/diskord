package com.serebit.diskord.data

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake

data class PermissionOverwriteData(
    val id: Snowflake,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)
