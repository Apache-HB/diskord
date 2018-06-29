package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.PermissionOverwriteData
import com.serebit.diskord.entities.Guild

class GuildVoiceChannel internal constructor(
    override val id: Snowflake,
    private var guild_id: Snowflake?,
    name: String,
    val position: Int,
    permission_overwrites: List<PermissionOverwriteData>,
    val bitrate: Int,
    val user_limit: Int,
    parent_id: Snowflake?
) : Channel {
    var name = name
        private set
    val guild: Guild? = guild_id?.let { EntityCache.find(it) }

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 2
    }
}
