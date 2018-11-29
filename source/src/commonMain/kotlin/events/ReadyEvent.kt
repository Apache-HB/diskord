package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.toDmChannel
import com.serebit.diskord.entities.toUser
import com.serebit.diskord.internal.caching.add
import com.serebit.diskord.internal.caching.addAll
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.payloads.dispatches.Ready

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = packet.user.toData(context).also {
        context.userCache.add(it)
    }.toUser()
    val dmChannels = packet.private_channels.map { it.toData(context) }.also {
        context.dmChannelCache.addAll(it)
    }.map { it.toDmChannel() }
}
