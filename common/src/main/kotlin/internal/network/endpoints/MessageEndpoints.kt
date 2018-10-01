package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.MessagePacket
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod

internal class GetMessage(channelId: Long, messageId: Long) :
    Endpoint<MessagePacket>(HttpMethod.Get, "/channels/$channelId/messages/$messageId", channelId)

internal class CreateMessage(channelId: Long) :
    Endpoint<MessagePacket>(HttpMethod.Post, "/channels/$channelId/messages", channelId)

internal class EditMessage(channelId: Long, messageId: Long) :
    Endpoint<MessagePacket>(HttpMethod.Patch, "/channels/$channelId/messages/$messageId", channelId)

internal class DeleteMessage(channelId: Long, messageId: Long) :
    Endpoint<HttpResponse>(HttpMethod.Delete, "/channels/$channelId/messages/$messageId", channelId)
