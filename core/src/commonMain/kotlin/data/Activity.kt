package com.serebit.strife.data

import com.serebit.strife.internal.packets.ActivityPacket
import com.soywiz.klock.DateTime

/**
 * @property name
 * @property type
 * @property appIconUrl
 * @property asset images for the presence and their hover texts
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
    val appIconUrl get() = "${cdnUri}app-icons/$applicationID/icon.png"

    enum class Type {
        Game, Streaming, Listening
    }

    /**
     * Images for the presence and their hover texts.
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
        val largeImageUrl get() = large_image_id?.let { "${cdnUri}app-assets/$appID/$it.png" }
        val smallImageUrl get() = small_image_id?.let { "${cdnUri}app-assets/$appID/$it.png" }
    }

    /**
     * Secrets for Rich Presence joining and spectating.
     * @property join the secret for joining a party
     * @property spectate the secret for spectating a game
     * @property match the secret for a specific instanced match
     */
    data class Secrets(val join: String? = null, val spectate: String? = null, val match: String? = null)

    data class TimeSpan(val start: DateTime? = null, val end: DateTime? = null)

    data class Party(val id: String? = null, val currentSize: Int? = null, val maxSize: Int? = null)

    companion object {
        const val cdnUri = "https://cdn.discordapp.com/"

        fun playing(name: String) = Activity(name, Type.Game)
        fun streaming(name: String) = Activity(name, Type.Streaming)
        fun listening(name: String) = Activity(name, Type.Listening)
    }
}

internal fun ActivityPacket.toActivity(): Activity = Activity(
    name, Activity.Type.values()[type], url,
    timestamps?.let { ts -> Activity.TimeSpan(ts.start?.let { DateTime.fromUnix(it) }, ts.end?.let {
        DateTime.fromUnix(it)
    }) },
    details, state, party?.let { Activity.Party(it.id, it.size?.get(0), it.size?.get(1)) }, application_id,
    assets?.let { Activity.Assets(application_id, it.large_image, it.small_image, it.large_text, it.small_text) },
    instance, secrets?.let { Activity.Secrets(it.join, it.spectate, it.match) }
)
