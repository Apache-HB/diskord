package com.serebit.strife.data

import com.serebit.strife.Context
import com.serebit.strife.entities.User
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.packets.MemberPacket
import com.serebit.strife.time.DateTime
import com.serebit.strife.time.toDateTime

class Member internal constructor(packet: MemberPacket, context: Context) {
    val user: User = context.userCache[packet.user.id]!!.toUser()
    val nickname: String? = packet.nick
    val joinedAt: DateTime = packet.joined_at.toDateTime()
    val isDeafened: Boolean = packet.deaf
    val isMuted: Boolean = packet.mute
}
