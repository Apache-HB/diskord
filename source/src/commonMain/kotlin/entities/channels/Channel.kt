package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.UnknownEntityTypeException
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.toMessage
import com.serebit.diskord.internal.entitydata.channels.ChannelData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.network.endpoints.CreateMessage
import com.serebit.diskord.time.DateTime

interface Channel : Entity

interface TextChannel : Channel {
    val lastMessage: Message?
    val lastPinTime: DateTime?

    fun send(message: String) = context.requester.requestObject(CreateMessage(id), data = mapOf("content" to message))
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
