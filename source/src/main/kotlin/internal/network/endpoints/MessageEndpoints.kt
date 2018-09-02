package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.internal.packets.MessagePacket
import org.http4k.core.Method
import org.http4k.core.Response

internal class GetMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint<MessagePacket>(Method.GET, "/channels/$channelId/messages/$messageId", channelId)

internal class CreateMessage(channelId: Snowflake) :
    Endpoint<MessagePacket>(Method.POST, "/channels/$channelId/messages", channelId)

internal class EditMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint<MessagePacket>(Method.PATCH, "/channels/$channelId/messages/$messageId", channelId)

internal class DeleteMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint<Response>(Method.DELETE, "/channels/$channelId/messages/$messageId", channelId)
