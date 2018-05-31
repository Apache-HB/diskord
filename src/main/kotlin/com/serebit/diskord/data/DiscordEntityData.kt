package com.serebit.diskord.data

import com.serebit.diskord.entities.DiscordEntity

internal interface DiscordEntityData<T : DiscordEntity> {
    fun toEntity(): T
}
