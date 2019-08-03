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
    override val lazyEntity by lazy { User(this) }
    var username = packet.username
        private set
    var discriminator = packet.discriminator
        private set
    var avatar = packet.avatar?.let { Avatar.Custom(id, it) } ?: Avatar.Default(discriminator)
        private set
    var status: UserStatus? = null
        private set
    var isBot = packet.bot
        private set

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
 * @property IDLE
 * @property DND
 * @property OFFLINE
 */
enum class OnlineStatus {
    /** The default state of a user when they are actively using Discord. Signified by a green circle. */
    ONLINE,
    /** The state of a user who has not interacted with their computer for some time. Signified by a yellow circle. */
    IDLE,
    /** Do Not Disturb. In this state, all notifications are silenced. Signified by a red circle. */
    DND,
    /**
     * The state of a user who is either not using Discord, or has manually set their status to "invisible".
     * Signified by a grey circle.
     */
    OFFLINE
}

/** The platform that the user is using Discord from. Each entry is a separate class of Discord client. */
enum class Platform {
    /** Discord on a desktop client, like the ones for Linux and Windows. */
    DESKTOP,
    /** Discord on a mobile device, like the iOS or Android apps. */
    MOBILE,
    /** Discord on the web, at https://discordapp.com. */
    WEB
}
