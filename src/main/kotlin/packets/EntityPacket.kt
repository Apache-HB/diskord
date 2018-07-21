package com.serebit.diskord.packets

import com.serebit.diskord.Snowflake

interface EntityPacket {
    val id: Snowflake
}
