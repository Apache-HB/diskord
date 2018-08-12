package com.serebit.diskord.packets

import com.serebit.diskord.Snowflake

internal data class UserPacket(
    val id: Snowflake,
    val username: String,
    val discriminator: Int,
    val avatar: String?,
    val bot: Boolean?,
    val mfa_enabled: Boolean?,
    val verified: Boolean?
)
