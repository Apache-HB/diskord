package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.data.Avatar
import com.serebit.strife.entities.User
import com.serebit.strife.internal.packets.UserPacket

internal class UserData(packet: UserPacket, override val context: Context) : EntityData<UserPacket, User> {
    override val id = packet.id
    var username = packet.username
    var discriminator = packet.discriminator
    var avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
    var isBot = packet.bot
    var hasMfaEnabled = packet.mfa_enabled
    var locale = packet.locale
    var isVerified = packet.verified
    var email = packet.email

    override fun update(packet: UserPacket) {
        username = packet.username
        discriminator = packet.discriminator
        avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
        isBot = packet.bot
        packet.mfa_enabled?.let { hasMfaEnabled = it }
        packet.locale?.let { locale = it }
        packet.verified?.let { isVerified = it }
        packet.email?.let { email = it }
    }

    override fun toEntity() = User(this)
}

internal fun UserPacket.toData(context: Context) = UserData(this, context)
