package com.serebit.diskord.data

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonDeserializer
import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCacher
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Channel
import com.serebit.diskord.entities.ChannelCategory
import com.serebit.diskord.entities.GroupTextChannel
import com.serebit.diskord.entities.GuildTextChannel
import com.serebit.diskord.entities.GuildVoiceChannel
import com.serebit.diskord.entities.PrivateTextChannel
import com.serebit.diskord.entities.UnknownChannel

internal interface ChannelData {
    val id: Snowflake
    fun toChannel(): Channel

    companion object {
        val deserializer = jsonDeserializer { (json, _, context) ->
            when (ChannelType.values().firstOrNull { it.value == json["type"].asInt }) {
                ChannelType.GUILD_TEXT -> context.deserialize<GuildTextChannelData>(json)
                ChannelType.DM -> context.deserialize<DmChannelData>(json)
                ChannelType.GROUP_DM -> context.deserialize<GroupDmChannelData>(json)
                ChannelType.GUILD_VOICE -> context.deserialize<GuildVoiceChannelData>(json)
                ChannelType.GUILD_CATEGORY -> context.deserialize<ChannelCategoryData>(json)
                null -> context.deserialize<UnknownChannelData>(json)
            }
        }
    }
}

internal data class GuildTextChannelData(
    override val id: Snowflake,
    val guild_id: Snowflake,
    val name: String,
    val position: Int,
    val permission_overwrites: List<PermissionOverwriteData>,
    val nsfw: Boolean,
    val topic: String?,
    val last_message_id: Snowflake,
    val parent_id: Snowflake?
) : ChannelData {
    override fun toChannel() = GuildTextChannel(
        id.toLong(),
        name,
        topic ?: "",
        position,
        nsfw
    )
}

internal data class DmChannelData(
    override val id: Snowflake,
    val recipients: List<UserData>
) : ChannelData {
    override fun toChannel(): Channel = PrivateTextChannel(
        id.toLong(),
        recipients.map { it.toUser() }
    )
}

internal data class GroupDmChannelData(
    override val id: Snowflake,
    val name: String,
    val recipients: List<UserData>,
    val owner_id: Snowflake,
    val icon: String?
) : ChannelData {
    override fun toChannel(): Channel = GroupTextChannel(
        id.toLong(),
        icon,
        recipients.map { it.toUser() },
        EntityCacher.findUser(owner_id)!!
    )
}

internal data class GuildVoiceChannelData(
    override val id: Snowflake,
    val guild_id: Snowflake,
    val name: String,
    val position: Int,
    val permission_overwrites: List<PermissionOverwriteData>,
    val bitrate: Int,
    val user_limit: Int,
    val parent_id: Snowflake?
) : ChannelData {
    override fun toChannel(): Channel = GuildVoiceChannel(
        id.toLong(),
        name,
        position,
        bitrate,
        user_limit
    )
}

internal data class ChannelCategoryData(
    override val id: Snowflake,
    val name: String,
    val position: Int,
    val guild_id: Snowflake
) : ChannelData {
    override fun toChannel(): Channel =
        ChannelCategory(id.toLong(), name, position)
}

internal data class UnknownChannelData(
    override val id: Snowflake
) : ChannelData {
    override fun toChannel(): Channel =
        UnknownChannel(id.toLong())
}

internal enum class ChannelType(val value: Int) {
    GUILD_TEXT(0), DM(1), GUILD_VOICE(2), GROUP_DM(3), GUILD_CATEGORY(4)
}

data class PermissionOverwriteData(
    val id: Snowflake,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)
