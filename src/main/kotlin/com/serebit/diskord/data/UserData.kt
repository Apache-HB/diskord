package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

data class UserData(
    val id: Snowflake,
    val username: String,
    val discriminator: String,
    val avatar: String?,
    val bot: Boolean?,
    val mfa_enabled: Boolean?,
    val verified: Boolean?
)
