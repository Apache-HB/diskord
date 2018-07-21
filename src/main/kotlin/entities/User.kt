package com.serebit.diskord.entities

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Avatar

class User internal constructor(
    override val id: Snowflake,
    username: String,
    discriminator: Int,
    avatar: String?,
    bot: Boolean?,
    mfa_enabled: Boolean?,
    verified: Boolean?
) : Entity {
    var username: String = username
        private set
    var discriminator: Int = discriminator
        private set
    var avatar = Avatar(id, discriminator, avatar)
    val isBot: Boolean = bot ?: false
    val isNormalUser: Boolean get() = !isBot
    var hasMfaEnabled: Boolean? = mfa_enabled
    var isVerified: Boolean? = verified

    init {
        EntityCache.cache(this)
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
