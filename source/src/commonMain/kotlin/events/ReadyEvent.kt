package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.channels.toDmChannel
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.caching.add
import com.serebit.strife.internal.caching.addAll
import com.serebit.strife.internal.entitydata.channels.toDmChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.dispatches.Ready

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = packet.user.toData(context).also {
        context.userCache.add(it)
    }.toUser()
    val dmChannels = packet.private_channels.map { it.toDmChannelData(context) }.also {
        context.dmChannelCache.addAll(it)
    }.map { it.toDmChannel() }
}
