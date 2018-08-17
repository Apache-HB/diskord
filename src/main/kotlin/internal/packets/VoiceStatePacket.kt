package com.serebit.diskord.internal.packets

import com.serebit.diskord.Snowflake

internal data class VoiceStatePacket(
    val guild_id: Snowflake?,
    val channel_id: Snowflake?,
    val user_id: Snowflake,
    val session_id: String,
    val deaf: Boolean,
    val mute: Boolean,
    val self_deaf: Boolean,
    val self_mute: Boolean,
    val suppress: Boolean
)
