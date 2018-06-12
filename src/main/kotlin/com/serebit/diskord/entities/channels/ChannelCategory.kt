package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild

class ChannelCategory internal constructor(
    override val id: Snowflake,
    var name: String,
    var position: Int,
    guild_id: Snowflake?
) : Channel {
    var guild: Guild? = guild_id?.let { EntityCache.find(guild_id)!! }
        private set

    init {
        EntityCache.cache(this)
    }
}
