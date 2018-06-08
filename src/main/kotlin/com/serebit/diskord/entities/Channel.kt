package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.network.ApiRequester

internal enum class TextChannelType(val value: Int) {
    GUILD_TEXT(0), DM(1), GROUP_DM(3)
}

data class PermissionOverwriteData(
    val id: Snowflake,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)

interface Channel : DiscordEntity

interface TextChannel : Channel {
    fun send(message: String) {
        val response = ApiRequester.post("/channels/$id/messages", data = mapOf("content" to message))
        println(response.text)
    }
}

class GuildVoiceChannel internal constructor(
    override val id: Snowflake,
    guild_id: Snowflake?,
    val name: String,
    val position: Int,
    permission_overwrites: List<PermissionOverwriteData>,
    val bitrate: Int,
    val user_limit: Int,
    parent_id: Snowflake?
) : Channel {
    val guild: Guild? = guild_id?.let { EntityCache.find(guild_id)!! }

    init {
        EntityCache.cache(this)
    }
}

class GuildTextChannel internal constructor(
    override val id: Snowflake,
    guild_id: Snowflake?,
    val name: String,
    val position: Int,
    permission_overwrites: List<PermissionOverwriteData>,
    val nsfw: Boolean,
    topic: String?,
    last_message_id: Snowflake,
    parent_id: Snowflake?
) : TextChannel {
    val guild: Guild? = guild_id?.let { EntityCache.find(guild_id)!! }
    val topic: String = topic ?: ""

    init {
        EntityCache.cache(this)
    }
}

class DmChannel internal constructor(
    override val id: Snowflake,
    val recipients: List<User>
) : TextChannel {
    init {
        EntityCache.cache(this)
    }
}

class GroupDmChannel internal constructor(
    override val id: Snowflake,
    val name: String,
    val recipients: List<User>,
    owner_id: Snowflake,
    icon: String?
) : TextChannel {
    val owner: User = recipients.first { it.id == owner_id }

    init {
        EntityCache.cache(this)
    }
}

class ChannelCategory internal constructor(
    override val id: Snowflake,
    val name: String,
    val position: Int,
    guild_id: Snowflake?
) : Channel {
    val guild: Guild? = guild_id?.let { EntityCache.find(guild_id)!! }

    init {
        EntityCache.cache(this)
    }
}

class UnknownChannel internal constructor(override val id: Snowflake) : Channel {
    init {
        EntityCache.cache(this)
    }
}
