package com.serebit.diskord.entities

import com.serebit.diskord.data.Avatar
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetUser
import com.serebit.diskord.internal.packets.UserPacket

class User internal constructor(packet: UserPacket) : Entity {
    override val id = packet.id
    var username: String = packet.username
        private set
    var discriminator: Int = packet.discriminator
        private set
    var avatar = Avatar(id, discriminator, packet.avatar)
        private set
    val isBot: Boolean = packet.bot ?: false
    val isNormalUser: Boolean get() = !isBot
    var hasMfaEnabled: Boolean? = packet.mfa_enabled
        private set
    var isVerified: Boolean? = packet.verified

    init {
        cache()
    }

    companion object {
        fun find(id: Long): User? = EntityCache.findId(id)
            ?: Requester.requestObject(GetUser(id))
                ?.let { User(it).cache() }
    }
}
