package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.data.Avatar

internal data class UserPacket(
    override val id: Long,
    val username: String,
    val discriminator: Int,
    private val avatar: String?,
    private val bot: Boolean?,
    val mfa_enabled: Boolean?,
    val verified: Boolean?
) : EntityPacket {
    val isBot by lazy { bot ?: false }
    val avatarObj by lazy { Avatar.from(id, discriminator, avatar) }
}

internal data class BasicUserPacket(override val id: Long) : EntityPacket

internal data class PresencePacket(
    val user: BasicUserPacket,
    val roles: List<Long>?,
    val game: ActivityPacket?,
    val guild_id: Long?,
    val status: String?
)

internal data class ActivityPacket(
    val name: String,
    val type: Int,
    val url: String?,
    val timestamps: Timestamps?,
    val application_id: Long?,
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
