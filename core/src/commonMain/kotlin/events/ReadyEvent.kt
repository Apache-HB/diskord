package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.internal.dispatches.Ready

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = packet.user.let { context.cache.pullUserData(it) }.toEntity()
    /** All [Private TextChannels][DmChannel] open to the client. */
    val dmChannels: List<DmChannel> = packet.private_channels
        .map { context.cache.pushChannelData(it).toEntity() as DmChannel }
}
