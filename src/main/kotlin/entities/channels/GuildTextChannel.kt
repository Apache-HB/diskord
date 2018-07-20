package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.PermissionOverwritePacket
import com.serebit.diskord.entities.Guild

class GuildTextChannel private constructor(
    override val id: Snowflake,
    private var guild_id: Snowflake?,
    private var parent_id: Snowflake?,
    name: String,
    position: Int,
    permission_overwrites: List<PermissionOverwritePacket>,
    nsfw: Boolean,
    topic: String?,
    last_message_id: Snowflake
) : TextChannel {
    val guild: Guild? get() = guild_id?.let { EntityCache.find(it) }
    val category: ChannelCategory? get() = parent_id?.let { EntityCache.find(it) }
    var name = name
        private set
    var position = position
        private set
    var topic = topic ?: ""
        private set
    var isNsfw = nsfw
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 0
    }
}
