package com.serebit.strife.data

import com.serebit.strife.Context
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.User
import com.serebit.strife.internal.ISO_WITHOUT_MS
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.packets.MemberPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse

/**
 * A [Member] is a [User] associated with a specific [Guild (aka server)][Guild]. A [Member] holds
 * data about the encased [User] which exists only in the respective [Guild].
 *
 * @constructor Builds a [Member] object from data within a [MemberPacket], [GuildData], and the relevant [Context]
 */
class Member internal constructor(packet: MemberPacket, guildData: GuildData, context: Context) {
    /** The [User] which owns this [Guild] as a [Member]. */
    val user: User = context.userCache[packet.user.id]!!.toEntity()
    /** guild The [Guild] in which this [Member] resides. */
    val guild: Guild = guildData.toEntity()
    /** An optional [nickname] which is used as an alias for the [User] in the [Guild]. */
    val nickname: String? = packet.nick
    /** The [User] which owns this [Guild] as a [Member]. */
    val joinedAt: DateTimeTz = try {
        DateFormat.ISO_WITH_MS.parse(packet.joined_at)
    } catch (ex: Exception) {
        DateFormat.ISO_WITHOUT_MS.parse(packet.joined_at)
    }
    /** whether the [Member] is deafened in [Voice Channels][GuildVoiceChannel]. */
    val isDeafened: Boolean = packet.deaf
    /** whether the [Member] is muted in [Voice Channels][GuildVoiceChannel]. */
    val isMuted: Boolean = packet.mute
}
