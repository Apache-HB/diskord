package com.serebit.diskord.entities

import java.time.Instant

private const val DISCORD_EPOCH = 1420070400000L
private const val CREATION_TIMESTAMP_BIT_DEPTH = 22

interface Entity {
    /**
     * A 64-bit unique identifier for a Discord entity. This ID is unique across all of Discord, except in scenarios
     * where a child entity inherits its parent's ID (such as a default channel in a guild created before the default
     * channel system was changed).
     */
    val id: Long
    /**
     * The date and time at which this entity was created. This information is baked into the entity's ID.
     */
    val createdAt: Instant get() = Instant.ofEpochMilli(DISCORD_EPOCH + (id shr CREATION_TIMESTAMP_BIT_DEPTH))
}
