package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.User

class GroupDmChannel internal constructor(
    override val id: Snowflake,
    name: String,
    recipients: List<User>,
    owner_id: Snowflake,
    icon: String?
) : TextChannel {
    var name: String = name
        private set
    var recipients = recipients
        private set
    var owner = recipients.first { it.id == owner_id }
        private set

    init {
        EntityCache.cache(this)
    }
}