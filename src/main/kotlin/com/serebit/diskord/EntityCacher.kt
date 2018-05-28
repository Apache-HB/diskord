package com.serebit.diskord

import com.serebit.diskord.entities.Channel
import com.serebit.diskord.gateway.Payload

internal object EntityCacher {
    val channelCache: MutableSet<Channel> = mutableSetOf()

    fun push(dispatch: Payload.Dispatch) {
        when (dispatch) {
            is Payload.Dispatch.MessageCreate -> {
                println("jeff")
            }
            is Payload.Dispatch.GuildCreate -> channelCache.addAll(dispatch.d.channels.mapNotNull { it.toChannel() })
        }
    }
}
