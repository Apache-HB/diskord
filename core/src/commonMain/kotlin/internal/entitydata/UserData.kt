package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.data.Avatar
import com.serebit.strife.entities.User
import com.serebit.strife.internal.entitydata.OnlineStatus.*
import com.serebit.strife.internal.entitydata.Platform.*
import com.serebit.strife.internal.packets.PresencePacket
import com.serebit.strife.internal.packets.UserPacket


internal class UserData(packet: UserPacket, override val context: Context) : EntityData<UserPacket, User> {
    override val id = packet.id
    var username = packet.username
    var discriminator = packet.discriminator
    var avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
    var status: UserStatus? = null
    var isBot = packet.bot

    override fun update(packet: UserPacket) {
        username = packet.username
        discriminator = packet.discriminator
        avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
        isBot = packet.bot
    }

    fun updateStatus(packet: PresencePacket) {
        this.status = UserStatus(
            OnlineStatus.valueOf(packet.status.toUpperCase()),
            when {
                packet.client_status.desktop != null -> Platform.DESKTOP
                packet.client_status.mobile != null -> Platform.MOBILE
                packet.client_status.web != null -> Platform.WEB
                else -> null
            }
        )
    }

    override fun toEntity() = User(this)
}

/** Returns this [UserPacket] as a [UserData] instance. */
internal fun UserPacket.toData(context: Context) = UserData(this, context)

/**
 * A [User]'s [OnlineStatus] and [Platform].
 *
 * @property onlineStatus The user's status: [IDLE], [DND], [ONLINE], or [OFFLINE ]
 * @property platform The user's digital platform: [DESKTOP], [MOBILE] or [WEB]
 */
data class UserStatus(val onlineStatus: OnlineStatus, val platform: Platform?)

enum class OnlineStatus { IDLE, DND, ONLINE, OFFLINE }

enum class Platform { DESKTOP, MOBILE, WEB }
