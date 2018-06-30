package com.serebit.diskord.entities.channels

import com.serebit.diskord.network.ApiEndpoint
import com.serebit.diskord.network.ApiRequester

interface TextChannel : Channel {
    fun send(message: String) =
        ApiRequester.requestObject(ApiEndpoint.createMessage(id), data = mapOf("content" to message))
}
