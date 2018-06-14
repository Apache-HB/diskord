package com.serebit.diskord.entities.channels

import com.fasterxml.jackson.annotation.JsonCreator
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.PermissionOverwriteData
import com.serebit.diskord.entities.Guild

class GuildTextChannel private constructor(
    override val id: Snowflake,
    private var guild_id: Snowflake?,
    private var parent_id: Snowflake?,
    name: String,
    position: Int,
    permission_overwrites: List<PermissionOverwriteData>,
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

    companion object {
        @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
        @JvmStatic
        internal fun create(
            id: Snowflake, guild_id: Snowflake?,
            name: String,
            position: Int,
            permission_overwrites: List<PermissionOverwriteData>,
            nsfw: Boolean,
            topic: String?,
            last_message_id: Snowflake,
            parent_id: Snowflake?
        ): GuildTextChannel = EntityCache.find<GuildTextChannel>(id)?.also {
            it.guild_id = guild_id
            it.name = name
            it.position = position
            it.topic = topic ?: ""
            it.isNsfw = nsfw
            it.parent_id = parent_id
        } ?: EntityCache.cache(
            GuildTextChannel(
                id, guild_id, parent_id, name, position, permission_overwrites, nsfw, topic, last_message_id
            )
        )
    }
}
