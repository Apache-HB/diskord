package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

class DmChannel private constructor(
    override val id: Snowflake,
    recipients: List<User>,
    last_message_id: Snowflake
) : TextChannel {
    var recipients: List<User> = recipients
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 1
    }
}
