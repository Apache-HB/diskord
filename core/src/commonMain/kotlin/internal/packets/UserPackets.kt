package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class UserPacket(
    override val id: Long,
    val username: String,
    val discriminator: Short,
    val avatar: String? = null,
    val bot: Boolean = false,
    val mfa_enabled: Boolean? = null,
    val locale: String? = null,
    val verified: Boolean? = null,
    val email: String? = null,
    val premium_type: Byte? = null
) : EntityPacket

@Serializable
internal data class BasicUserPacket(override val id: Long) : EntityPacket

@Serializable
internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Long> = emptyList(),
    val game: ActivityPacket?,
    val guild_id: Long? = null,
    val status: String,
    val activities: List<ActivityPacket>
)

/**
 * The user's activity, i.e., playing, streaming, listening.
 *
 * [see](https://discordapp.com/developers/docs/topics/gateway#activity-object-activity-structure)
 */
@Serializable
internal data class ActivityPacket(
    val name: String,
    val type: Byte,
    val url: String? = null,
    val timestamps: Timestamps? = null,
    val application_id: Long? = null,
    val details: String? = null,
    val state: String? = null,
    val party: Party? = null,
    val assets: Assets? = null,
    val secrets: Secrets? = null,
    val instance: Boolean? = null,
    val flags: Short = 0
) {
    @Serializable
    data class Timestamps(
        val start: Long? = null,
        val end: Long? = null
    )

    // size is a list of two integers, the first being the current party size and the second being the max size
    @Serializable
    data class Party(
        val id: String? = null,
        val size: List<Int>? = null
    )

    @Serializable
    data class Assets(
        val large_image: String? = null,
        val large_text: String? = null,
        val small_image: String? = null,
        val small_text: String? = null
    )

    @Serializable
    data class Secrets(
        val join: String? = null,
        val spectate: String? = null,
        val match: String? = null
    )

    enum class Flags(val value: Int) {
        INSTANCE(1 shl 0), JOIN(1 shl 1), SPECTATE(1 shl 2), JOIN_REQUEST(1 shl 3), SYNC(1 shl 4), PLAY(1 shl 5)
    }
}
