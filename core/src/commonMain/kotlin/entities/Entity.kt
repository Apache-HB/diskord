package com.serebit.strife.entities

import com.serebit.strife.Context
import com.soywiz.klock.DateTime

internal const val DISCORD_EPOCH = 1420070400000L
private const val CREATION_TIMESTAMP_BIT_DEPTH = 22

/**
 * A Discord Entity is any object with a unique identifier in the form of a 64-bit integer.
 * This unique identifier contains basic information about the entity.
 */
interface Entity {
    /**
     * A 64-bit unique identifier for a Discord entity. This ID is unique across all of Discord, except in scenarios
     * where a child entity inherits its parent's ID (such as a default channel in a guild created before the default
     * channel system was changed).
     */
    val id: Long
    /** The date and time at which this entity was created. This information is baked into the entity's ID. */
    val createdAt: DateTime get() = DateTime(DISCORD_EPOCH + (id shr CREATION_TIMESTAMP_BIT_DEPTH))
    /** The [Context] this [Entity] exists within. */
    val context: Context
}
