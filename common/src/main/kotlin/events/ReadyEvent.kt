package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.DmChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.payloads.dispatches.Ready

class ReadyEvent internal constructor(override val context: Context, packet: Ready.Data) : Event {
    val user = User(packet.user).cache()
    val dmChannels: List<DmChannel> = packet.private_channels.map { DmChannel(it).cache() }
}