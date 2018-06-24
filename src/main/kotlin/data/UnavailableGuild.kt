package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

internal data class UnavailableGuild(val unavailable: Boolean, val id: Snowflake)
