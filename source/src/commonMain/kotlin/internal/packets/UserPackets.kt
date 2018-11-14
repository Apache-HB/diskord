package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.data.Avatar
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class UserPacket(
    override val id: Long,
    val username: String,
    val discriminator: Int,
    private val avatar: String? = null,
    @Optional private val bot: Boolean? = null,
    @Optional val mfa_enabled: Boolean? = null,
    @Optional val locale: String? = null,
    @Optional val verified: Boolean? = null,
    @Optional val email: String? = null
) : EntityPacket {
    @Transient
    val isBot by lazy { bot ?: false }
    @Transient
    val avatarObj by lazy { Avatar.from(id, discriminator, avatar) }
}

@Serializable
internal data class BasicUserPacket(override val id: Long) : EntityPacket

@Serializable
internal data class PresencePacket(
    val user: BasicUserPacket,
    @Optional val roles: List<Long> = emptyList(),
    val game: ActivityPacket?,
    val guild_id: Long,
    val status: String,
    val activities: List<ActivityPacket>
)

@Serializable
internal data class ActivityPacket(
    val name: String,
    val type: Int,
    @Optional val url: String? = null,
    @Optional val timestamps: Timestamps? = null,
    @Optional val application_id: Long? = null,
    @Optional val details: String? = null,
    @Optional val state: String? = null,
    @Optional val party: Party? = null,
    @Optional val assets: Assets? = null,
    @Optional val secrets: Secrets? = null,
    @Optional val instance: Boolean? = null,
    @Optional val flags: BitSet = 0
) {
    data class Timestamps(
        @Optional val start: UnixTimestamp? = null,
        @Optional val end: UnixTimestamp? = null
    )

    // size is a list of two integers, the first being the current party size and the second being the max size
    data class Party(
        @Optional val id: String? = null,
        @Optional val size: List<Int>? = null
    )

    data class Assets(
        @Optional val large_image: String? = null,
        @Optional val large_text: String? = null,
        @Optional val small_image: String? = null,
        @Optional val small_text: String? = null
    )

    data class Secrets(
        @Optional val join: String? = null,
        @Optional val spectate: String? = null,
        @Optional val match: String? = null
    )

    enum class Flags(val value: Int) {
        INSTANCE(1 shl 0), JOIN(1 shl 1), SPECTATE(1 shl 2), JOIN_REQUEST(1 shl 3), SYNC(1 shl 4), PLAY(1 shl 5)
    }
}
