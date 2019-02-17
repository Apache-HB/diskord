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

class Member internal constructor(packet: MemberPacket, guildData: GuildData, context: Context) {
    val user: User = context.userCache[packet.user.id]!!.toEntity()
    val guild: Guild = guildData.toEntity()
    val nickname: String? = packet.nick
    val joinedAt: DateTimeTz = try {
        DateFormat.ISO_WITH_MS.parse(packet.joined_at)
    } catch (ex: Exception) {
        DateFormat.ISO_WITHOUT_MS.parse(packet.joined_at)
    }
    val isDeafened: Boolean = packet.deaf
    val isMuted: Boolean = packet.mute
}
