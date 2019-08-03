package com.serebit.strife.data

import com.serebit.strife.BotClient
import com.serebit.strife.data.Activity.Type.*
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.User
import com.serebit.strife.internal.network.Cdn
import com.serebit.strife.internal.network.ImageFormat
import com.serebit.strife.internal.packets.ActivityPacket
import com.serebit.strife.internal.packets.PresencePacket
import com.soywiz.klock.DateTimeTz
import kotlin.experimental.or

/**
 * A [User]'s [Presence] is their current state on a [guild], including their [onlineStatus], [game] and [activities].
 * Discord separates [presences][Presence] per-guild. While a [User] is unlikely to have a different [Presence] in
 * another guild, we cannot ensure that as they're separated in the API.
 */
class Presence internal constructor(packet: PresencePacket, val guild: Guild, val context: BotClient) {
    /** The ID of the [User]. */
    val userID: Long = packet.user.id
    /** The current [OnlineStatus] of the [User]. */
    val onlineStatus: OnlineStatus = packet.status.toStatus()
    /** The [User]'s [online statuses][OnlineStatus] across different clients. */
    val clientStatus = ClientStatus(packet.client_status)
    /** The [User]'s current [Activity], or `null` if the [User] doesn't have any activity. */
    val game: Activity? = packet.game?.let { Activity(it) }
    /** A list of the [User]'s [activities][Activity]. */
    val activities: List<Activity> = packet.activities.map { Activity(it) }

    /**
     * Get the [GuildMember] this [Presence] belongs to. Returns the [GuildMember], or `null` if we don't have access
     * to the member.
     */
    suspend fun getMember(): GuildMember? = guild.getMember(userID)

    /**
     * Get the [User] this [Presence] belongs to. Returns the [User], or `null` if we don't have access
     * to the user.
     */
    suspend fun getUser(): User? = context.getUser(userID)

    /** A class containing a [User]'s [online statuses][OnlineStatus] across different clients. */
    class ClientStatus internal constructor(packet: PresencePacket.ClientStatusPacket) {
        /** The [User]'s current [OnlineStatus] on a desktop client, like the ones for Linux and Windows. */
        val desktop: OnlineStatus = packet.desktop.toStatus()
        /** The [User]'s current [OnlineStatus] on a mobile device, like the iOS or Android apps. */
        val mobile: OnlineStatus = packet.mobile.toStatus()
        /** The [User]'s current [OnlineStatus] on on the web, at [https://discordapp.com]. */
        val web: OnlineStatus = packet.web.toStatus()
    }
}

internal fun PresencePacket.toPresence(guild: Guild, context: BotClient) = Presence(this, guild, context)

/**
 * A [User]'s [OnlineStatus] is a online status indicator that shows how they are currently using Discord. This can be
 * set manually, or controlled automatically by Discord.
 */
enum class OnlineStatus {
    /** The default state of a user when they are actively using Discord. Signified by a green circle. */
    ONLINE,
    /** The state of a user who has not interacted with their computer for some time. Signified by a yellow circle. */
    IDLE,
    /** Do Not Disturb. In this state, all notifications are silenced. Signified by a red circle. */
    DND,
    /**
     * The state of a user who is either not using Discord, or has manually set their onlineStatus to "invisible".
     * Signified by a grey circle.
     */
    OFFLINE
}

private fun String?.toStatus() = this?.let { OnlineStatus.valueOf(it.toUpperCase()) } ?: OnlineStatus.OFFLINE

/**
 * A User's [Activity] is the information shown in their profile about their current game/stream/etc.
 *
 * [See the entry in the Discord API docs.](https://discordapp.com/developers/docs/topics/gateway#activity-object)
 */
class Activity internal constructor(packet: ActivityPacket) {
    /** The name of this [Activity]. */
    val name: String = packet.name
    /**
     * The [Type] of this [Activity]: [Playing][Type.Playing], [Streaming][Type.Streaming] or
     * [Listening][Type.Listening].
     */
    val type: Type = Type.values().getOrNull(packet.type) ?: Type.Playing
    /** The url of a [streaming activity][Type.Streaming]. */
    val url: String? = packet.url
    /** The [TimeSpan] from [start][TimeSpan.start] to [end][TimeSpan.end] of this [Activity]. */
    val timespan: TimeSpan? = packet.timestamps?.let { TimeSpan(it) }
    /** The ID of the [game][Type.Playing] the [User] is currently playing. */
    val gameID: Long? = packet.application_id
    /** What the [User] is currently doing, shown as the first line in the [Activity]. */
    val details: String? = packet.details
    /** The [User]'s current party status, shown as the second line in the [Activity]. */
    val state: String? = packet.state
    /** The [User]'s current [Party]. */
    val party = packet.party?.let { Party(it) }
    /** The images of this [Activity] and their hover texts. */
    val assets = packet.assets?.let { Assets(it, gameID) }
    /** The [Secrets] for Rich Presence joining and spectating. */
    val secrets = packet.secrets?.let { Secrets(it) }
    /** `true` if this [Activity] is an instanced game session. */
    val instance = packet.instance
    /** A list of [flags][Flag] for this [Activity]. */
    val flags: List<Flag> = Flag.values().filter { it.value.or(packet.flags) == packet.flags }

    /** The type of [Activity]: [Playing], [Streaming], [Listening], or [Watching]. */
    enum class Type {
        /** Playing a game. Shown as "Playing [name][Activity.name]". */
        Playing,
        /** Streaming on Twitch. Shown as "Streaming [name][Activity.name]". */
        Streaming,
        /** Listening to... something you can listen to. Shown as "Listening to [name][Activity.name]". */
        Listening,
        /** Watching something, like a video or stream. Shown as "Watching [name][Activity.name]". */
        Watching
    }

    /** The time span of an [Activity] from [start] to [end]. */
    class TimeSpan internal constructor(packet: ActivityPacket.Timestamps) {
        /** The starting time of the [Activity], or `null` if no starting time was set. */
        val start: DateTimeTz? = packet.start?.let { DateTimeTz.fromUnixLocal(it) }
        /** The ending time of the [Activity], or `null` if no ending time was set. */
        val end: DateTimeTz? = packet.end?.let { DateTimeTz.fromUnixLocal(it) }
    }

    /**
     * The party (group of players) of an [Activity] (usually [playing][Type.Playing] or [listening][Type.Listening] to
     * Spotify).
     */
    class Party internal constructor(packet: ActivityPacket.Party) {
        /** The ID of this [Party]. */
        val id = packet.id
        /** The current size of this [Party], or `null` if this party doesn't have a size. */
        val currentSize = packet.size?.get(0)
        /** The maximum size of this [Party], or `null` if this party doesn't have a size. */
        val maxSize = packet.size?.get(1)
    }

    /** The images of an [Activity] and their hover texts. */
    class Assets internal constructor(packet: ActivityPacket.Assets, applicationID: Long?) {
        /** The url for the large image of the [Activity], or `null` if there is no image. */
        val largeImage: String? =
            packet.large_image?.let { imageID ->
                applicationID?.let { Cdn.ApplicationAsset(it, imageID, ImageFormat.Png).toString() }
                    ?: "https://i.scdn.co/image/" + imageID.substringAfter("spotify:")
            }
        /**
         * The text displayed when hovering over the [largeImage] of the [Activity], or `null` if there is no image.
         */
        val largeText: String? = packet.large_image

        /** The url for the small image of the [Activity], or `null` if there is no image. */
        val smallImage: String? =
            packet.small_image?.let { Cdn.ApplicationAsset(applicationID!!, it, ImageFormat.Png).toString() }
        /**
         * The text displayed when hovering over the [smallImage] of the [Activity], or `null` if there is no image.
         */
        val smallText: String? = packet.small_text
    }

    /** The [Secrets] for Rich Presence joining and spectating. */
    class Secrets internal constructor(packet: ActivityPacket.Secrets) {
        /** The secret for joining a [Party], or `null` if not available. */
        val join: String? = packet.join
        /** The secret for spectating a game, or `null` if not available. */
        val spectate: String? = packet.spectate
        /** The secret for a specific instanced match, or `null` if not available. */
        val match: String? = packet.match
    }

    enum class Flag(internal val value: Short) {
        INSTANCE(1 shl 0),
        JOIN(1 shl 1),
        SPECTATE(1 shl 2),
        JOIN_REQUEST(1 shl 3),
        SYNC(1 shl 4),
        PLAY(1 shl 5)
    }
}
