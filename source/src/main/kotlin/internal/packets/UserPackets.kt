package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp

internal data class UserPacket(
    val id: Snowflake,
    val username: String,
    val discriminator: Int,
    val avatar: String?,
    val bot: Boolean?,
    val mfa_enabled: Boolean?,
    val verified: Boolean?
)

internal data class BasicUserPacket(val id: Snowflake)

internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Snowflake>?,
    val game: ActivityPacket?,
    val guild_id: Snowflake?,
    val status: String?
)

internal data class ActivityPacket(
    val name: String,
    val type: Int,
    val url: String?,
    val timestamps: Timestamps?,
    val application_id: Snowflake?,
    val details: String?,
    val state: String?,
    val party: Party?,
    val assets: Assets?,
    val secrets: Secrets?,
    val instance: Boolean?,
    val flags: BitSet
) {
    data class Timestamps(val start: UnixTimestamp?, val end: UnixTimestamp?)

    // size is a list of two integers, the first being the current party size and the second being the max size
    data class Party(val id: String?, val size: List<Int>?)

    data class Assets(
        val large_image: String?,
        val large_text: String?,
        val small_image: String?,
        val small_text: String?
    )

    data class Secrets(
        val join: String?,
        val spectate: String?,
        val match: String?
    )

    enum class Flags(val value: Int) {
        INSTANCE(1 shl 0), JOIN(1 shl 1), SPECTATE(1 shl 2), JOIN_REQUEST(1 shl 3), SYNC(1 shl 4), PLAY(1 shl 5)
    }
}