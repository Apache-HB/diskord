package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.DiscordEntityData
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

class GuildVoiceChannel internal constructor(data: Data) : Channel {
    override val id: Long = data.id
    private val guildId: Long = data.guild_id
    val guild: Guild get() = EntityCache.find(guildId)!!

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val guild_id: Snowflake,
        private val name: String,
        private val position: Int,
        val permission_overwrites: List<PermissionOverwriteData>,
        private val bitrate: Int,
        private val user_limit: Int,
        val parent_id: Snowflake?
    ) : DiscordEntityData
}

class GuildTextChannel internal constructor(data: Data) : TextChannel {
    override val id: Long = data.id
    val name: String = data.name
    private val guildId: Long = data.guild_id
    val guild: Guild get() = EntityCache.find(guildId)!!
    val topic: String = data.topic ?: ""
    val position: Int = data.position
    val isNsfw: Boolean = data.nsfw

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val guild_id: Snowflake,
        val name: String,
        val position: Int,
        val permission_overwrites: List<PermissionOverwriteData>,
        val nsfw: Boolean,
        val topic: String?,
        val last_message_id: Snowflake,
        val parent_id: Snowflake?
    ) : DiscordEntityData
}

class DmChannel internal constructor(data: Data) : TextChannel {
    override val id: Long = data.id
    val recipients: List<User> = data.recipients

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val recipients: List<User>
    ) : DiscordEntityData
}

class GroupDmChannel internal constructor(data: Data) : TextChannel {
    override val id: Long = data.id
    val name: String = data.name
    val icon: String? = data.icon
    val recipients: List<User> = data.recipients
    val owner: User = data.recipients.first { it.id == data.owner_id }

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val name: String,
        val recipients: List<User>,
        val owner_id: Snowflake,
        val icon: String?
    ) : DiscordEntityData
}

class ChannelCategory internal constructor(data: Data) : Channel {
    override val id: Long = data.id
    val name: String = data.name
    val position: Int = data.position

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake,
        val name: String,
        val position: Int,
        val guild_id: Snowflake
    ) : DiscordEntityData
}

class UnknownChannel internal constructor(data: Data) : Channel {
    override val id: Long = data.id

    init {
        EntityCache.cache(this)
    }

    internal data class Data(
        override val id: Snowflake
    ) : DiscordEntityData
}
