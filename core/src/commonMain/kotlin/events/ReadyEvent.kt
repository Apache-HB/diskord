package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.add
import com.serebit.strife.internal.entitydata.addAll
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.entitydata.toDmChannelData

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = packet.user.toData(context).also {
        context.userCache.add(it)
    }.toEntity()
    val dmChannels: List<DmChannel> = packet.private_channels
        .map { it.toDmChannelData(context).also { } }
        .also { context.dmCache.addAll(it) }
        .map { it.toEntity() }
}
