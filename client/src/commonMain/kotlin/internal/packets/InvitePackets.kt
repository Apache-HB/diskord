package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class InvitePacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelPacket,
    val approximate_presence_count: Int? = null,
    val approximate_member_count: Int? = null
)

@Serializable
internal data class InviteMetadataPacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelPacket,
    val approximate_presence_count: Int? = null,
    val approximate_member_count: Int? = null,
    val inviter: UserPacket,
    val uses: Int,
    val max_uses: Int,
    val max_age: Int,
    val temporary: Boolean,
    val created_at: String,
    val revoked: Boolean
)
