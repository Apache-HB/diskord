package com.serebit.diskord.data

class Avatar internal constructor(id: Long, discriminator: Int, hash: String?) {
    val isCustom = hash != null
    val isDefault = !isCustom
    val isAnimated = hash != null && hash.startsWith("a_")
    private val fileExtension = if (hash != null && isAnimated) "gif" else "png"
    val uri = if (isCustom) {
        "https://cdn.discordapp.com/avatars/$id/$hash.$fileExtension"
    } else {
        "https://cdn.discordapp.com/embed/avatars/${discriminator % NUM_DEFAULT_AVATARS}.png"
    }

    companion object {
        const val NUM_DEFAULT_AVATARS = 5
    }
}
