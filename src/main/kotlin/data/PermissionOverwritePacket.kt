package com.serebit.diskord.data

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake

internal data class PermissionOverwritePacket(
    val id: Snowflake,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)
