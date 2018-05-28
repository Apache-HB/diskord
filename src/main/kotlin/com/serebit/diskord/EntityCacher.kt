package com.serebit.diskord

import com.serebit.diskord.entities.Channel
import com.serebit.diskord.entities.User
import com.serebit.diskord.gateway.Payload

internal object EntityCacher {
    val channelCache: MutableSet<Channel> = mutableSetOf()
    val userCache: MutableSet<User> = mutableSetOf()

    fun push(dispatch: Payload.Dispatch) {
        when (dispatch) {
            is Payload.Dispatch.MessageCreate -> {
                userCache += dispatch.d.author.toUser()
            }
            is Payload.Dispatch.GuildCreate -> {
                userCache += dispatch.d.members.map { it.user.toUser() }
                channelCache += dispatch.d.channels.mapNotNull { it.toChannel() }
            }
        }
    }
}
