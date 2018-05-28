package com.serebit.diskord.data

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Channel
import com.serebit.diskord.entities.GuildTextChannel

internal data class ChannelData(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val position: Int?,
    val permission_overwrites: List<PermissionOverwriteData>?,
    val name: String?,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Snowflake?,
    val bitrate: Int?,
    val user_limit: Int?,
    val recipients: Int?,
    val icon: String?,
    val owner_id: Snowflake?,
    val application_id: Snowflake?,
    val parent_id: Snowflake?,
    val last_pin_timestamp: IsoTimestamp?
) {
    enum class Type(val id: Int) {
        GUILD_TEXT(0), DM(1), GUILD_VOICE(2), GROUP_DM(3), GUILD_CATEGORY(4)
    }

    data class PermissionOverwriteData(
        val id: Snowflake,
        val type: String,
        val allow: BitSet,
        val deny: BitSet
    )

    fun toChannel(): Channel? = when(type) {
        0 -> GuildTextChannel(id.toLong(), name!!, topic ?: "", position!!, nsfw ?: false)
        else -> null
    }
}
