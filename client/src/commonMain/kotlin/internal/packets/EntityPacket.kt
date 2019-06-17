package com.serebit.strife.internal.packets

/**
 * A packet of data received through the
 * [Discord Gateway](https://discordapp.com/developers/docs/topics/gateway#gateways) regarding an
 * [Entity][com.serebit.strife.entities.Entity].
 */
internal interface EntityPacket {
    /** The Snowflake ID of the [Entity][com.serebit.strife.entities.Entity]. */
    val id: Long
}
