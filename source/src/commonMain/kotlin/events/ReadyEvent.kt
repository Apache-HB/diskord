package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.DmChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.cacheAll
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.payloads.dispatches.Ready

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = User(packet.user.cache().id, context)
    val dmChannels: List<DmChannel> = packet.private_channels.cacheAll().map { DmChannel(it.id, context) }

    init {
        context.cache.cache(packet.user.toData(context))
        context.cache.cacheAll(packet.private_channels.map { it.toData(context) })
    }
}
