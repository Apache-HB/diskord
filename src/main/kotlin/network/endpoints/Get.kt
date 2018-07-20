@file:JvmName("GetEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import khttp.responses.Response

internal object GetGatewayBot : ApiEndpoint.Get<Response>("/gateway/bot")
internal class GetChannel(channelId: Snowflake) : ApiEndpoint.Get<Channel>("/channels/$channelId", setOf(channelId))
internal class GetMessage(channelId: Snowflake, messageId: Snowflake)
    : ApiEndpoint.Get<Message>("/channels/$channelId/messages/$messageId", setOf(channelId))
internal class GetUser(userId: Snowflake) : ApiEndpoint.Get<User>("/users/$userId")
internal class GetGuild(guildId: Snowflake) : ApiEndpoint.Get<Guild>("/guilds/$guildId", setOf(guildId))
