package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Message
import org.http4k.core.Method

internal class GetMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint<Message>(Method.GET, "/channels/$channelId/messages/$messageId", channelId)

internal class CreateMessage(channelId: Snowflake) :
    Endpoint<Message>(Method.POST, "/channels/$channelId/messages", channelId)

internal class EditMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint<Message>(Method.PATCH, "/channels/$channelId/messages/$messageId", channelId)