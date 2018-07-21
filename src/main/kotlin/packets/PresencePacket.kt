package com.serebit.diskord.packets

import com.serebit.diskord.Snowflake

internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Snowflake>?,
    val game: ActivityPacket?,
    val guild_id: Snowflake?,
    val status: String?
)
