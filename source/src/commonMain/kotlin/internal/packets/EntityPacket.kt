package com.serebit.diskord.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal interface EntityPacket {
    val id: Long
}
