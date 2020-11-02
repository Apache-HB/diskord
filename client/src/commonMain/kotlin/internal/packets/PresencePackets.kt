package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Long> = emptyList(),
    val game: ActivityPacket? = null,
    val guild_id: Long? = null,
    val status: String,
    val activities: List<ActivityPacket>,
    val client_status: ClientStatusPacket
) {
    @Serializable
    data class ClientStatusPacket(val desktop: String? = null, val mobile: String? = null, val web: String? = null)
}

/** [see](https://discordapp.com/developers/docs/topics/gateway#activity-object-activity-structure) */
@Serializable
internal data class ActivityPacket(
    val name: String,
    val type: Int,
    val url: String? = null,
    val timestamps: Timestamps? = null,
    val application_id: Long? = null,
    val details: String? = null,
    val state: String? = null,
    val emoji: Emoji? = null,
    val party: Party? = null,
    val assets: Assets? = null,
    val secrets: Secrets? = null,
    val instance: Boolean? = null,
    val flags: Short = 0
) {
    @Serializable
    data class Timestamps(val start: Long? = null, val end: Long? = null)

    // size is a list of two integers, the first being the current party size and the second being the max size
    @Serializable
    data class Party(val id: String? = null, val size: List<Int>? = null)

    @Serializable
    data class Assets(
        val large_image: String? = null,
        val large_text: String? = null,
        val small_image: String? = null,
        val small_text: String? = null
    )

    @Serializable
    data class Secrets(val join: String? = null, val spectate: String? = null, val match: String? = null)

    @Serializable
    data class Emoji(val name: String, val id: Long? = null, val animated: Boolean? = null)
}
