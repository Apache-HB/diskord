package com.serebit.diskord.data

import com.serebit.diskord.Snowflake

class Avatar internal constructor(id: Snowflake, discriminator: Int, hash: String?) {
    val isCustom = hash != null
    val isDefault = !isCustom
    val isAnimated = hash != null && hash.startsWith("a_")
    private val fileExtension = if (hash != null && isAnimated) "gif" else "png"
    val uri = if (isCustom) {
        "https://cdn.discordapp.com/avatars/$id/$hash.$fileExtension"
    } else {
        "https://cdn.discordapp.com/embed/avatars/${discriminator % 5}.png"
    }
}
