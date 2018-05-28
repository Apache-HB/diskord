package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

data class EmoteData(
    val id: Snowflake?,
    val name: String,
    val roles: List<Snowflake>,
    val user: UserData?,
    val require_colons: Boolean?,
    val managed: Boolean?,
    val animated: Boolean?
)
