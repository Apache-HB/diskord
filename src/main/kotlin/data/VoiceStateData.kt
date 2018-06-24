package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

data class VoiceStateData(
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
