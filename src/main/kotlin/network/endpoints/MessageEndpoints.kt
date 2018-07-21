@file:JvmName("MessageEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Message

internal class GetMessage(channelId: Snowflake, messageId: Snowflake) :
    Endpoint.Get<Message>("/channels/$channelId/messages/$messageId", channelId)

internal class CreateMessage(channelId: Snowflake) : Endpoint.Post<Message>("/channels/$channelId/messages", channelId)
