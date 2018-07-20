package com.serebit.diskord.data

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

internal data class EmotePacket(
    val id: Snowflake?,
    val name: String,
    val roles: List<Snowflake>,
    val user: User?,
    val require_colons: Boolean?,
    val managed: Boolean?,
    val animated: Boolean?
)
