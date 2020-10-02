package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.Invite
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@Serializable
internal data class ChannelID(val id: Long)

@Serializable
internal data class InvitePacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelID,
    val target_user: UserPacket? = null,
    val target_user_type: Int? = null,
    val approximate_presence_count: Int? = null,
    val approximate_member_count: Int? = null
)

@Serializable
internal data class InviteMetadataPacket(
    val code: String,
    val guild: PartialGuildPacket? = null,
    val channel: ChannelID,
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
    val revoked: Boolean = false
)

@OptIn(ExperimentalTime::class)
internal fun InviteMetadataPacket.toInvite(context: BotClient, guild: Guild, member: GuildMember?) = Invite(
    code,
    uses,
    max_uses,
    guild,
    guild.getChannel(channel.id)!!,
    member,
    target_user?.let { context.cache.pullUserData(it) }?.lazyEntity,
    Instant.parse(created_at).let { it..it.plus(max_age.seconds) },
    approximate_presence_count,
    approximate_member_count,
    temporary,
    revoked
)
