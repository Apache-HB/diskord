package com.serebit.diskord.entities.channels

import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.endpoints.CreateMessage

interface TextChannel : Channel {
    fun send(message: String) = ApiRequester.requestObject(CreateMessage(id), data = mapOf("content" to message))
}
