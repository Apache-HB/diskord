package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.Invite
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.soywiz.klock.seconds
import kotlinx.serialization.Serializable

@Serializable
internal data class InvitePacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelPacket,
    val target_user: UserPacket? = null,
    val target_user_type: Int? = null,
    val approximate_presence_count: Int? = null,
    val approximate_member_count: Int? = null
)

@Serializable
internal data class InviteMetadataPacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelPacket,
    val target_user: UserPacket? = null,
    val target_user_type: Int? = null,
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

internal suspend fun InviteMetadataPacket.toInvite(context: BotClient, guild: Guild) = Invite(
    code,
    uses,
    max_uses,
    guild,
    guild.getChannel(channel.id)!!,
    guild.getMember(inviter.id),
    target_user?.let { context.cache.pullUserData(it) }?.lazyEntity,
    try {
        DateFormat.ISO_WITH_MS.parse(created_at)
    } catch (ex: Exception) {
        DateFormat.ISO_WITHOUT_MS.parse(created_at)
    }.let { it..(it + max_age.seconds) },
    approximate_presence_count,
    approximate_member_count,
    temporary,
    revoked
)
