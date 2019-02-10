package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.channels.DmChannel
import com.serebit.strife.entities.channels.toDmChannel
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.channels.toDmChannelData
import com.serebit.strife.internal.entitydata.toData

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = packet.user.toData(context).also {
        context.userCache + (it.id to it)
    }.toUser()
    val dmChannels: List<DmChannel> = packet.private_channels
        .map { it.toDmChannelData(context) }
        .also { l -> context.dmCache.putAll(l.associate { it.id to it }) }
        .map { it.toDmChannel() }
}
