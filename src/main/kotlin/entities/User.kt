package com.serebit.diskord.entities

import com.fasterxml.jackson.annotation.JsonCreator
import com.serebit.diskord.BitSet
import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp

class User internal constructor(
    override val id: Snowflake,
    username: String,
    discriminator: Int,
    avatar: String?,
    bot: Boolean?,
    mfa_enabled: Boolean?,
    verified: Boolean?
) : DiscordEntity {
    var username: String = username
        private set
    var discriminator: Int = discriminator
        private set
    var avatar: String = avatar ?: DefaultAvatar.valueOf(discriminator).uri
        set(value) {
            field = "https://cdn.discordapp.com/avatars/$id/$value${if (value.startsWith("a_")) ".gif" else ".png"}"
        }
    val isBot: Boolean = bot ?: false
    val isNormalUser: Boolean get() = !isBot
    var hasMfaEnabled: Boolean? = mfa_enabled
    var isVerified: Boolean? = verified

    companion object {
        @Suppress("LongParameterList")
        @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
        @JvmStatic
        fun create(
            id: Snowflake,
            username: String,
            discriminator: Int,
            avatar: String?,
            bot: Boolean?,
            mfa_enabled: Boolean?,
            verified: Boolean?
        ): User = EntityCache.find<User>(id)?.also { user ->
            user.username = username
            user.discriminator = discriminator
            user.avatar = avatar ?: DefaultAvatar.valueOf(discriminator).uri
            mfa_enabled?.let { user.hasMfaEnabled = it }
            verified?.let { user.isVerified = it }
        } ?: EntityCache.cache(User(id, username, discriminator, avatar, bot, mfa_enabled, verified))
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
