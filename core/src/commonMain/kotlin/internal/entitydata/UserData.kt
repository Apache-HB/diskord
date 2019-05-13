package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.entities.User
import com.serebit.strife.internal.entitydata.OnlineStatus.*
import com.serebit.strife.internal.entitydata.Platform.*
import com.serebit.strife.internal.packets.PresencePacket
import com.serebit.strife.internal.packets.UserPacket

internal class UserData(packet: UserPacket, override val context: BotClient) : EntityData<UserPacket, User> {
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
                packet.client_status.desktop != null -> DESKTOP
                packet.client_status.mobile != null -> MOBILE
                packet.client_status.web != null -> WEB
                else -> null
            }
        )
    }

    override fun toEntity() = User(this)
}

/** Returns this [UserPacket] as a [UserData] instance. */
internal fun UserPacket.toData(context: BotClient) = UserData(this, context)

/**
 * A [User]'s [OnlineStatus] and [Platform].
 *
 * @property onlineStatus The user's status: [IDLE], [DND], [ONLINE], or [OFFLINE]
 * @property platform The user's digital platform: [DESKTOP], [MOBILE] or [WEB]
 */
data class UserStatus(val onlineStatus: OnlineStatus, val platform: Platform?)

/**
 * A [User]'s [OnlineStatus] is a status indicator that shows how they are currently using Discord. This can be set
 * manually, or controlled automatically by Discord.
 *
 * @property ONLINE The default state of a user when they are actively using Discord. Signified by a green circle.
 * @property IDLE The state of a user who has not interacted with their computer for some time. Signified by a yellow
 * circle.
 * @property DND Do Not Disturb, or the state of a user who doesn't want to be disturbed. In this state, all
 * notifications are silenced. Signified by a red circle.
 * @property OFFLINE The state of a user who is either not using Discord from any client, or has manually set their
 * status as such. Signified by a grey circle.
 */
enum class OnlineStatus {
    ONLINE,
    IDLE,
    DND,
    OFFLINE
}

enum class Platform { DESKTOP, MOBILE, WEB }
