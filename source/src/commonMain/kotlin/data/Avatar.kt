package com.serebit.diskord.data

/**
 * An image avatar representing a Discord user. When a new account is created, the user is given a default avatar,
 * with a background color based on their randomly generated discriminator. After this, the user can set their own
 * custom avatar. This can be a regular still image, or in the case of Discord Nitro users, an animated GIF.
 */
class Avatar private constructor(id: Long, discriminator: Int, hash: String?) {
    /**
     * Returns true if this avatar is custom, meaning that it is an image uploaded to Discord and not one of the
     * default avatars.
     */
    val isCustom = hash != null
    /**
     * Returns true if this avatar is one of the five default avatars.
     */
    val isDefault get() = !isCustom
    /**
     * Returns true if this avatar is animated. Animated avatars are only available for Discord Nitro users.
     */
    val isAnimated = hash != null && hash.startsWith("a_")
    private val fileExtension = if (isAnimated) "gif" else "png"
    /**
     * The URI for the avatar image. If the avatar is custom, this will point to the Discord CDN location for the
     * custom image; otherwise, this will point to the Discord CDN location for the user's default avatar.
     */
    val uri by lazy {
        if (isCustom) "$CUSTOM_AVATAR_ROOT/$id/$hash.$fileExtension"
        else "$DEFAULT_AVATAR_ROOT/${discriminator % NUM_DEFAULT_AVATARS}.png"
    }

    private constructor(defaultAvatarIndex: Int) : this(0, defaultAvatarIndex, null)

    companion object {
        const val NUM_DEFAULT_AVATARS = 5

        val DEFAULT_BLURPLE = Avatar(0)
        val DEFAULT_GREY = Avatar(1)
        val DEFAULT_GREEN = Avatar(2)
        val DEFAULT_ORANGE = Avatar(3)
        val DEFAULT_RED = Avatar(4)
        private const val DEFAULT_AVATAR_ROOT = "https://cdn.discordapp.com/embed/avatars"
        private const val CUSTOM_AVATAR_ROOT = "https://cdn.discordapp.com/avatars"

        fun from(id: Long, discriminator: Int, hash: String?): Avatar =
            if (hash == null) when (discriminator % NUM_DEFAULT_AVATARS) {
                0 -> DEFAULT_BLURPLE
                1 -> DEFAULT_GREY
                2 -> DEFAULT_GREEN
                3 -> DEFAULT_ORANGE
                else -> DEFAULT_RED
            } else Avatar(id, discriminator, hash)
    }
}
