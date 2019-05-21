package com.serebit.strife.data

import com.serebit.strife.internal.network.encodeBase64

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
    /** `true` if this avatar is animated. *Animated avatars are only available for Discord Nitro users.* */
    abstract val isAnimated: Boolean

    /**
     * A custom image uploaded by the user. This can be either an animated GIF, or a static image.
     */
    class Custom internal constructor(id: Long, hash: String) : Avatar() {
        override val isAnimated: Boolean = hash.startsWith("a_")
        override val uri: String = "$CUSTOM_AVATAR_ROOT/$id/$hash.${if (isAnimated) "gif" else "png"}"

        override fun equals(other: Any?): Boolean = other is Custom && other.uri == uri

        companion object {
            private const val CUSTOM_AVATAR_ROOT = "https://cdn.discordapp.com/avatars"
        }
    }

    /**
     * One of Discord's default avatars, selected from the user's discriminator. They all appear as the plain white
     * Discord logo on a solid color background.
     * There are 5 default avatars, each with their own background color.
     *
     * @property backgroundColor The solid background color of the image.
     */
    sealed class Default(index: Byte, val backgroundColor: Color) : Avatar() {
        override val uri: String = "$DEFAULT_AVATAR_ROOT/$index.png"
        override val isAnimated: Boolean = false

        /** The blurple default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/0.png). */
        object BLURPLE : Default(0, Color.BLURPLE)

        /** The grey default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/1.png). */
        object GREY : Default(1, Color(0x747F8D))

        /** The green default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/2.png). */
        object GREEN : Default(2, Color(0x43B581))

        /** The orange default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/3.png). */
        object ORANGE : Default(3, Color(0xFAA61A))

        /** The red default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/4.png). */
        object RED : Default(4, Color(0xF04747))

        companion object {
            private const val DEFAULT_AVATAR_ROOT = "https://cdn.discordapp.com/embed/avatars"
            /** The number of unique default avatars that Discord has on offer. */
            const val NUM_DEFAULT_AVATARS: Int = 5

            internal operator fun invoke(discriminator: Short) =
                when (val i = discriminator % NUM_DEFAULT_AVATARS) {
                    0 -> BLURPLE
                    1 -> GREY
                    2 -> GREEN
                    3 -> ORANGE
                    4 -> RED
                    else -> throw IllegalStateException("No default avatar available at index $i.")
                }
        }
    }
}

/**
 * A class providing the avatar data necessary to change the self user's avatar.
 * Supports JPG, PNG, and GIF formats.
 */
class AvatarData private constructor(type: String, imageData: ByteArray) {
    internal val dataUri by lazy {
        "data:image/$type;base64,${encodeBase64(imageData)}"
    }

    companion object {
        /** Returns an [AvatarData] instance with jpg format and the [imageData] provided. */
        fun jpg(imageData: ByteArray): AvatarData = AvatarData("jpeg", imageData)

        /** Returns an [AvatarData] instance with png format and the [imageData] provided. */
        fun png(imageData: ByteArray): AvatarData = AvatarData("png", imageData)

        /** Returns an [AvatarData] instance with jpg format and the [imageData] provided. */
        fun gif(imageData: ByteArray): AvatarData = AvatarData("gif", imageData)
    }
}
