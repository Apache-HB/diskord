package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.UnknownEntityTypeException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.network.endpoints.CreateMessage

interface TextChannel : Channel {
    val lastMessage: Message?
    val lastPinTime: DateTime?

    fun send(message: String) = context.requester.requestObject(CreateMessage(id), data = mapOf("content" to message))
        ?.cache()
        ?.let { Message(it.id, it.channel_id, context) }
}

internal fun TextChannelData.toTextChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}
