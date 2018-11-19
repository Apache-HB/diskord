package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.entities.Message
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.CreateMessage

interface TextChannel : Channel {
    val lastMessage: Message?
    val lastPinTime: DateTime?

    fun send(message: String) = Requester.requestObject(CreateMessage(id), mapOf(), mapOf("content" to message))
        ?.cache()
        ?.let { Message(it.id, it.channel_id) }
}
