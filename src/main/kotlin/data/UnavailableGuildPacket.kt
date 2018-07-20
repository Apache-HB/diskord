package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

internal data class UnavailableGuildPacket(val unavailable: Boolean, val id: Snowflake)
