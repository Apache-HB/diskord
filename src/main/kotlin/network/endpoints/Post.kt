@file:JvmName("PostEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Message

internal class CreateMessage(channelId: Snowflake) :
    ApiEndpoint.Post<Message>("/channels/$channelId/messages", setOf(channelId))
