package com.serebit.diskord.packets

import com.serebit.diskord.Snowflake

internal data class UnavailableGuildPacket(val unavailable: Boolean, val id: Snowflake)
