package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.entities.User
import com.serebit.strife.internal.packets.UserPacket

internal class UserData(packet: UserPacket, override val context: BotClient) : EntityData<UserPacket, User> {
    override val id = packet.id
    override val lazyEntity by lazy { User(this) }
    var username = packet.username
        private set
    var discriminator = packet.discriminator
        private set
    var avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
        private set
    var isBot = packet.bot
        private set

    override fun update(packet: UserPacket) {
        username = packet.username
        discriminator = packet.discriminator
        avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
        isBot = packet.bot
    }
}

internal fun UserPacket.toData(context: BotClient) = UserData(this, context)
