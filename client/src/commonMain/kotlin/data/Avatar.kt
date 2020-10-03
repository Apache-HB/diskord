package com.serebit.strife.data

import com.serebit.strife.internal.encodeBase64
import com.serebit.strife.internal.network.Cdn
import com.serebit.strife.internal.network.ImageFormat

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
        override val uri: String =
            Cdn.UserAvatar(id, hash, if (isAnimated) ImageFormat.Gif else ImageFormat.Png).toString()

        /** Checks if this custom avatar is equivalent to the [given object][other]. */
        override fun equals(other: Any?): Boolean = other is Custom && other.uri == uri
    }

    /**
     * One of Discord's default avatars, selected from the user's discriminator. They all appear as the plain white
     * Discord logo on a solid color background.
     * There are 5 default avatars, each with their own background color.
     *
     * @property backgroundColor The solid background color of the image.
     */
    sealed class Default(index: Byte, val backgroundColor: Color) : Avatar() {
        override val uri: String = Cdn.DefaultUserAvatar(index).toString()
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
            /** The number of unique default avatars that Discord offers. */
            const val NUM_DEFAULT_AVATARS: Int = 5

            internal operator fun invoke(discriminator: Short) = when (val i = discriminator % NUM_DEFAULT_AVATARS) {
                0 -> BLURPLE
                1 -> GREY
                2 -> GREEN
                3 -> ORANGE
                4 -> RED
                else -> error("No default avatar available at index $i.")
            }
        }
    }
}

/**
 * A class providing the avatar data necessary to change the self user's avatar. See [AvatarFormat] for supported
 * formats.
 */
class AvatarData private constructor(format: AvatarFormat, imageData: ByteArray) {
    /** A lazy function providing the uri that can be passed to Discord API to change the self user's avatar. */
    internal val dataUri = "data:image/${format.name.toLowerCase()};base64,${encodeBase64(imageData)}"
}

/** An enum containing supported [AvatarData] formats that can be used to change self user's avatar. */
enum class AvatarFormat {
    /** A still image with significantly lossy compression and no support for alpha transparency. Smallest file size. */
    Jpeg,

    /** A still image with slightly lossy compression and support for alpha transparency. */
    Png,

    /** A still or animated image with a limited color palette. Largest file size and worst quality. */
    Gif
}
