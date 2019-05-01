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
    /** `True` if this avatar is animated. *Animated avatars are only available for Discord Nitro users.* */
    abstract val isAnimated: Boolean

    /**
     * A Custom [Avatar] uploaded by the user.
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
     * One of [NUM_DEFAULT_AVATARS] default avatars, selected from the user's discriminator. They all appear as the
     * plain white Discord logo on a solid color background.
     * There are 5 [Default] avatars: [BLURPLE], [GREY], [GREEN], [ORANGE], & [RED].
     *
     * @property backgroundColor The solid background color of the image.
     */
    class Default private constructor(index: Byte, val backgroundColor: Color) : Avatar() {
        override val uri = "$DEFAULT_AVATAR_ROOT/$index.png"
        override val isAnimated = false

        override fun equals(other: Any?) = other is Default && other.uri == uri

        companion object {
            private const val DEFAULT_AVATAR_ROOT = "https://cdn.discordapp.com/embed/avatars"
            /** The number of unique default avatars that Discord has on offer. */
            const val NUM_DEFAULT_AVATARS = 5

            /** The blurple default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/0.png). */
            val BLURPLE = Default(0, Color.BLURPLE)
            /** The grey default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/1.png). */
            val GREY = Default(1, Color(0x747F8D))
            /** The green default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/2.png). */
            val GREEN = Default(2, Color(0x43B581))
            /** The orange default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/3.png). */
            val ORANGE = Default(3, Color(0xFAA61A))
            /** The red default avatar, which looks like [this](https://cdn.discordapp.com/embed/avatars/4.png). */
            val RED = Default(4, Color(0xF04747))

            internal operator fun invoke(discriminator: Short) = when (val i = discriminator % NUM_DEFAULT_AVATARS) {
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
 * Supports [jpg] formats.
 */
class AvatarData internal constructor(type: String, imageData: ByteArray) {
    val dataUri = "data:image/$type;base64,${encodeBase64(imageData)}"

    companion object {
        /** Returns an [AvatarData] instance with jpg format and the [imageData] provided. */
        fun jpg(imageData: ByteArray): AvatarData = AvatarData("jpeg", imageData)

        /** Returns an [AvatarData] instance with png format and the [imageData] provided. */
        fun png(imageData: ByteArray): AvatarData = AvatarData("png", imageData)

        /** Returns an [AvatarData] instance with jpg format and the [imageData] provided. */
        fun gif(imageData: ByteArray): AvatarData = AvatarData("gif", imageData)
    }
}

internal expect fun encodeBase64(bytes: ByteArray): String