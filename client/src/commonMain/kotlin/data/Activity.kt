package com.serebit.strife.data

import com.serebit.strife.data.Activity.Party
import com.serebit.strife.data.Activity.Type.*
import com.serebit.strife.internal.network.Cdn
import com.serebit.strife.internal.network.ImageFormat
import com.serebit.strife.internal.packets.ActivityPacket
import com.soywiz.klock.DateTime

/**
 * A User's [Activity] is the information shown in their profile about their current game/stream/etc.
 *
 * See [the entry in the Discord API docs.](https://discordapp.com/developers/docs/topics/gateway#activity-object)
 *
 * @property name The name of the activity.
 * @property type The type of activity: Game, Streaming or Listening
 * @property url The url of a [streaming activity][Activity.Type.Streaming].
 * @property timespan The time span from start to end of the activity.
 * @property details What the player is currently doing.
 * @property state The user's current party status.
 * @property party Information for the current [Party] of the player.
 * @property applicationID
 * @property asset Images for the presence and their hover texts
 * @property isInstance `true` if the activity is an instanced game session.
 * @property secrets Secrets for Rich Presence joining and spectating.
 */
data class Activity internal constructor(
    val name: String,
    val type: Type,
    val url: String? = null,
    val timespan: TimeSpan? = null,
    val details: String? = null,
    val state: String? = null,
    val party: Party? = null,
    private val applicationID: Long? = null,
    val asset: Assets? = null,
    val isInstance: Boolean? = null,
    val secrets: Secrets? = null
) {
    /** The type of [Activity]: [Game], [Streaming], or [Listening]. */
    enum class Type {
        /** Playing a game. Shown as "Playing [name][Activity.name]". */
        Game,
        /** Streaming on Twitch. Shown as "Streaming [name][Activity.name]". */
        Streaming,
        /** Listening to...something you can listen to. Shown as "Listening to [name][Activity.name]" .*/
        Listening
    }

    /**
     * Images for the presence and their hover texts.
     *
     * @property largeImageUrl the url for a large asset of the activity.
     * @property largeText text displayed when hovering over the large image of the activity.
     * @property smallImageUrl the url for a small asset of the activity.
     * @property smallText text displayed when hovering over the small image of the activity.
     */
    class Assets internal constructor(
        private val appID: Long?,
        private val large_image_id: String? = null,
        private val small_image_id: String? = null,
        val largeText: String? = null,
        val smallText: String? = null
    ) {
        val largeImageUrl: String?
            get() = large_image_id?.let { imageId ->
                appID?.let { Cdn.ApplicationAsset(it, imageId, ImageFormat.Png).toString() }
            }
        val smallImageUrl: String?
            get() = small_image_id?.let { imageId ->
                appID?.let { Cdn.ApplicationAsset(it, imageId, ImageFormat.Png).toString() }
            }
    }

    /**
     * Secrets for Rich Presence joining and spectating.
     *
     * @property join the secret for joining a party
     * @property spectate the secret for spectating a game
     * @property match the secret for a specific instanced match
     */
    data class Secrets(val join: String? = null, val spectate: String? = null, val match: String? = null)

    /**
     * The [start] and [end] times of the [Activity].
     *
     * @property start The starting time, or null if no starting time was set.
     * @property end The ending time, or null if no ending time was set.
     */
    data class TimeSpan(val start: DateTime? = null, val end: DateTime? = null)

    /**
     * The party (group of players) of the [Activity] (usually gaming or listening to Spotify).
     *
     * @property id The party ID.
     * @property currentSize The current size of the party.
     * @property maxSize The maximum size of the party.
     */
    data class Party(val id: String? = null, val currentSize: Int? = null, val maxSize: Int? = null)

    companion object {
        /** Create an activity with the given [name] and [type]. */
        operator fun invoke(name: String, type: Type): Activity = Activity(name, type)
    }
}

internal fun ActivityPacket.toActivity(): Activity = Activity(
    name,
    values()[if (type in 0..values().size) type else 0],
    url,
    timestamps?.let { ts ->
        Activity.TimeSpan(ts.start?.let { DateTime.fromUnix(it) }, ts.end?.let {
            DateTime.fromUnix(it)
        })
    },
    details,
    state,
    party?.let { Party(it.id, it.size?.get(0), it.size?.get(1)) },
    application_id,
    assets?.let { Activity.Assets(application_id, it.large_image, it.small_image, it.large_text, it.small_text) },
    instance,
    secrets?.let { Activity.Secrets(it.join, it.spectate, it.match) }
)
