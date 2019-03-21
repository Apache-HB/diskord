package com.serebit.strife.data

/**
 * An image avatar representing a Discord user. When a new account is created, the user is given a default avatar,
 * with a background color based on their randomly generated discriminator. After this, the user can set their own
 * custom avatar. This can be a regular still image, or in the case of Discord Nitro users, an animated GIF.
 */
sealed class Avatar {
    /**
     * The URI for the avatar image. If the avatar is custom, this will point to the Discord CDN location for the
     * custom image; otherwise, this will point to the Discord CDN location for the user's default avatar.
     */
    abstract val uri: String
    /** True if this avatar is animated. *Animated avatars are only available for Discord Nitro users.* */
    abstract val isAnimated: Boolean

    /**
     * A Custom [Avatar] uploaded by the [user][com.serebit.strife.entities.User].
     * Each [Custom Avatar][Custom] has an [Long] id and a hash [String] which are used to build the
     * [URI][Avatar.uri] of the Avatar link.
     */
    class Custom internal constructor(id: Long, hash: String) : Avatar() {
        override val isAnimated = hash.startsWith("a_")
        override val uri = "$CUSTOM_AVATAR_ROOT/$id/$hash.${if (isAnimated) "gif" else "png"}"

        override fun equals(other: Any?) = other is Custom && other.uri == uri

        companion object {
            private const val CUSTOM_AVATAR_ROOT = "https://cdn.discordapp.com/avatars"
        }
    }

    /**
     * A Default [Avatar] created by Discord as a place holder for users who have not uploaded a
     * [Custom avatar][Custom]. There are 5 [Default] avatars: [BLURPLE], [GREY], [GREEN], [ORANGE], & [RED].
     */
    class Default internal constructor(discriminator: Short) : Avatar() {
        override val uri = "$DEFAULT_AVATAR_ROOT/${discriminator % NUM_DEFAULT_AVATARS}.png"
        override val isAnimated = false

        override fun equals(other: Any?) = other is Default && other.uri == uri

        companion object {
            private const val DEFAULT_AVATAR_ROOT = "https://cdn.discordapp.com/embed/avatars"
            /** The number of unique default avatars that Discord has on offer. */
            const val NUM_DEFAULT_AVATARS = 5

            /** The blurple default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/0.png). */
            val BLURPLE = Default(0)
            /** The grey default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/1.png). */
            val GREY = Default(1)
            /** The green default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/2.png). */
            val GREEN = Default(2)
            /** The orange default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/3.png). */
            val ORANGE = Default(3)
            /** The red default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/4.png). */
            val RED = Default(4)
        }
    }
}
