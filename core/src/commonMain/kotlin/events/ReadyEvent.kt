package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.entities.User
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.add
import com.serebit.strife.internal.entitydata.addAll
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.entitydata.toDmChannelData

/**
 * The [ReadyEvent] is dispatched when a client has completed the initial handshake with the [Gateway]. The
 * [ReadyEvent] can be the largest and most complex [Event] the [Gateway] will send, as it contains all the state
 * required for a client to begin interacting with the rest of the platform.
 */
class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    /** The client as a [User] */
    val user: User = packet.user.toData(context).also { context.userCache.add(it) }.toEntity()
    /** An empty list. */
    val dmChannels: List<DmChannel> = packet.private_channels
        .map { it.toDmChannelData(context) }
        .also { context.dmCache.addAll(it) }
        .map { it.toEntity() }
}
