package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.MessagePacket
import io.ktor.http.HttpMethod

internal class GetMessage(channelId: Long, messageId: Long) : Endpoint.Object<MessagePacket>(
    HttpMethod.Get, "/channels/$channelId/messages/$messageId", MessagePacket.serializer(),
    channelId
)

internal class CreateMessage(channelId: Long) : Endpoint.Object<MessagePacket>(
    HttpMethod.Post, "channels/$channelId/messages", MessagePacket.serializer(),
    channelId
)

internal class EditMessage(channelId: Long, messageId: Long) : Endpoint.Object<MessagePacket>(
    HttpMethod.Patch, "channels/$channelId/messages/$messageId", MessagePacket.serializer(),
    channelId
)

internal class DeleteMessage(channelId: Long, messageId: Long) : Endpoint.Response(
    HttpMethod.Delete, "channels/$channelId/messages/$messageId",
    channelId
)
