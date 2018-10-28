package com.serebit.diskord.entities

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetUser
import com.serebit.diskord.internal.packets.UserPacket

/**
 * Represents a Discord user, whether a person or bot.
 */
data class User internal constructor(override val id: Long) : Entity {
    private val packet: UserPacket
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetUser(id))
            ?: throw EntityNotFoundException("Invalid user instantiated with ID $id.")
    val username: String get() = packet.username
    val discriminator: Int get() = packet.discriminator
    val avatar get() = packet.avatarObj
    val isBot: Boolean get() = packet.isBot
    val isNormalUser: Boolean get() = !isBot
    val hasMfaEnabled: Boolean? get() = packet.mfa_enabled
    val isVerified: Boolean? get() = packet.verified
}
