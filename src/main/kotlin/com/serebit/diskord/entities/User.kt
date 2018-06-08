package com.serebit.diskord.entities

import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp

class User internal constructor(
    override val id: Snowflake,
    val username: String,
    val discriminator: Int,
    avatar: String?,
    bot: Boolean?,
    val mfa_enabled: Boolean?,
    val verified: Boolean?
) : DiscordEntity {
    val avatar: String = avatar?.let {
        "https://cdn.discordapp.com/avatars/$id/$it${if (it.startsWith("a_")) ".gif" else ".png"}"
    } ?: DefaultAvatar.valueOf(discriminator).uri
    val isBot: Boolean = bot ?: false
    val isNormalUser: Boolean get() = !isBot

    init {
        EntityCache.cache(this)
    }

    data class ActivityData(
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
        data class Party(val id: String?, val size: List<Int>)

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

    private enum class DefaultAvatar(id: String) {
        BLURPLE("6debd47ed13483642cf09e832ed0bc1b"),
        GREY("322c936a8c8be1b803cd94861bdfa868"),
        GREEN("dd4dbc0016779df1378e7812eabaa04d"),
        ORANGE("0e291f67c9274a1abdddeb3fd919cbaa"),
        RED("1cbd08c76f8af6dddce02c5138971129");

        val uri = "https://cdn.discordapp.com/assets/$id.png"

        companion object {
            fun valueOf(discriminator: Int): DefaultAvatar = values()[discriminator % values().size]
        }
    }
}
