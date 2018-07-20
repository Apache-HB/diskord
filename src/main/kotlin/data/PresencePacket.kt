package com.serebit.diskord.data

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Snowflake>?,
    val game: User.ActivityData?,
    val guild_id: Snowflake?,
    val status: String?
)
