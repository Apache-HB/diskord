package com.serebit.diskord.data

import com.serebit.diskord.Context
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.toUser
import com.serebit.diskord.internal.packets.MemberPacket

class Member internal constructor(packet: MemberPacket, context: Context) {
    val user: User = context.userCache[packet.user.id]!!.toUser()
    val nickname: String? = packet.nick
    val joinedAt: DateTime = packet.joined_at.toDateTime()
    val isDeafened: Boolean = packet.deaf
    val isMuted: Boolean = packet.mute
}
