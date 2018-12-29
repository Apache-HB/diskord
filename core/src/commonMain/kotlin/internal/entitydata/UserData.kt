package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.data.Avatar
import com.serebit.strife.internal.packets.UserPacket

internal class UserData(packet: UserPacket, override val context: Context) : EntityData {
    override val id = packet.id
    var username = packet.username
    var discriminator = packet.discriminator
    var avatar = Avatar.from(id, discriminator, packet.avatar)
    var isBot = packet.bot
    var hasMfaEnabled = packet.mfa_enabled
    var locale = packet.locale
    var isVerified = packet.verified
    var email = packet.email

    fun update(packet: UserPacket) = apply {
        username = packet.username
        discriminator = packet.discriminator
        avatar = Avatar.from(id, discriminator, packet.avatar)
        isBot = packet.bot
        packet.mfa_enabled?.let { hasMfaEnabled = it }
        packet.locale?.let { locale = it }
        packet.verified?.let { isVerified = it }
        packet.email?.let { email = it }
    }
}

internal fun UserPacket.toData(context: Context) = UserData(this, context)
