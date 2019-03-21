package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class VoiceStatePacket(
    val guild_id: Long? = null,
    val channel_id: Long? = null,
    val user_id: Long,
    val session_id: String,
    val deaf: Boolean,
    val mute: Boolean,
    val self_deaf: Boolean,
    val self_mute: Boolean,
    val suppress: Boolean
)
