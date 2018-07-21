package com.serebit.diskord.entities.channels

import com.serebit.diskord.network.Requester
import com.serebit.diskord.network.endpoints.CreateMessage

interface TextChannel : Channel {
    fun send(message: String) = Requester.requestObject(CreateMessage(id), data = mapOf("content" to message))
}
