package com.serebit.strife.entities.channels

import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.entities.Entity
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.toMessage
import com.serebit.strife.internal.entitydata.channels.*
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Endpoint
import com.soywiz.klock.DateTimeTz

interface Channel : Entity

interface TextChannel : Channel {
    val lastMessage: Message?
    val lastPinTime: DateTimeTz?

    suspend fun send(message: String) =
        context.requester.sendRequest(Endpoint.CreateMessage(id), data = mapOf("content" to message))
            .value
            ?.toData(context)
            ?.toMessage()
}

internal fun ChannelData.toChannel() = when (this) {
    is GuildChannelData -> toGuildChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}

internal fun TextChannelData.toTextChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}
