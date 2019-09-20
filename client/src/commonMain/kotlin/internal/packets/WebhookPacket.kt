package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class WebhookPacket(
    val id: Long,
    val guild_id: Long? = null,
    val channel_id: Long,
    val user: UserPacket? = null,
    val name: String?,
    val avatar: String?,
    val token: String
)
