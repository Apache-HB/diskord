package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.MessageCreatePacket
import io.ktor.http.HttpMethod

internal class GetMessage(channelId: Long, messageId: Long) : Endpoint.ObjectData<MessageCreatePacket>(
    HttpMethod.Get, "/channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(),
    channelId
)

internal class CreateMessage(channelId: Long) : Endpoint.ObjectData<MessageCreatePacket>(
    HttpMethod.Post, "channels/$channelId/messages", MessageCreatePacket.serializer(),
    channelId
)

internal class EditMessage(channelId: Long, messageId: Long) : Endpoint.ObjectData<MessageCreatePacket>(
    HttpMethod.Patch, "channels/$channelId/messages/$messageId", MessageCreatePacket.serializer(),
    channelId
)

internal class DeleteMessage(channelId: Long, messageId: Long) : Endpoint.NoData(
    HttpMethod.Delete, "channels/$channelId/messages/$messageId",
    channelId
)
