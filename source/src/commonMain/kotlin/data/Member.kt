package com.serebit.diskord.data

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.MemberPacket

class Member internal constructor(packet: MemberPacket, context: Context) {
    val user: User = User(packet.user.cache().id, context)
    val nickname: String? = packet.nick
    val roles: List<Role> = packet.roles.map { Role(it, context) }
    val joinedAt: DateTime = packet.joined_at.toDateTime()
    val isDeafened: Boolean = packet.deaf
    val isMuted: Boolean = packet.mute
}
