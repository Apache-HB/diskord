package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class ModifyChannelPacket(
    val name: String? = null,
    val position: Int? = null,
    val topic: String? = null,
    val nsfw: Boolean? = null,
    val rate_limit_per_user: Int? = null,
    val bitrate: Int? = null,
    val user_limit: Int? = null,
    val permission_overwrites: List<PermissionOverwritePacket>? = null,
    val parent_id: Long? = null
)

@Serializable
internal data class GetChannelMessagesPacket(
    val around: Long? = null,
    val before: Long? = null,
    val after: Long? = null,
    val limit: Int? = null
)
