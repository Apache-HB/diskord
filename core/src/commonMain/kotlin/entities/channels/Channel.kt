package com.serebit.strife.entities.channels

import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.entities.Entity
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.toMessage
import com.serebit.strife.internal.entitydata.channels.ChannelData
import com.serebit.strife.internal.entitydata.channels.DmChannelData
import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import com.serebit.strife.internal.entitydata.channels.GuildTextChannelData
import com.serebit.strife.internal.entitydata.channels.TextChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Endpoint.CreateMessage
import com.soywiz.klock.DateTimeTz

/** Represents a guild or DM [channel](https://discordapp.com/developers/docs/resources/channel) within Discord. */
interface Channel : Entity

/** A [Channel] used to send textual, image, or other messages. */
interface TextChannel : Channel {
    /** The most recent [Message] in the [TextChannel]. */
    val lastMessage: Message?
    /** The [date and time][DateTimeTz] of the last pinned [Message]. */
    val lastPinTime: DateTimeTz?

    /**
     * Send a new [Message] to this [TextChannel].
     * @return the sent [Message] or null if it failed to send.
     */
    suspend fun send(message: String): Message? =
        context.requester.sendRequest(CreateMessage(id), data = mapOf("content" to message))
            .value?.toData(context)?.toMessage()
}

/** Encapsulate a [ChannelData] object in an end-user facing [Channel]. */
internal fun ChannelData.toChannel() = when (this) {
    is GuildChannelData -> toGuildChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}

/** Encapsulate a [TextChannelData] object in an end-user facing [TextChannel]. */
internal fun TextChannelData.toTextChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}
