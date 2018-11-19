package com.serebit.diskord.data

import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.MemberPacket

class Member internal constructor(packet: MemberPacket) {
    val user: User = User(packet.user.cache().id)
    val nickname: String? = packet.nick
    val roles: List<Role> = packet.roles.map(::Role)
    val joinedAt: DateTime = DateTime.fromIsoTimestamp(packet.joined_at)
    val isDeafened: Boolean = packet.deaf
    val isMuted: Boolean = packet.mute
}
