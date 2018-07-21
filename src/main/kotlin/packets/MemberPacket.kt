package com.serebit.diskord.packets

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

internal data class MemberPacket(
    val user: User,
    val nick: String?,
    val roles: List<Snowflake>,
    val joined_at: IsoTimestamp,
    val deaf: Boolean,
    val mute: Boolean
)
