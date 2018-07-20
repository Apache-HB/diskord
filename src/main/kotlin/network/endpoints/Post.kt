@file:JvmName("PostEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.Role
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.DmChannel

internal class CreateMessage(channelId: Snowflake) :
    ApiEndpoint.Post<Message>("/channels/$channelId/messages", setOf(channelId))
internal object CreateDmChannel :
    ApiEndpoint.Post<DmChannel>("/users/@me/channels")
internal object CreateGuild :
    ApiEndpoint.Post<Guild>("/guilds")
internal class CreateGuildChannel(guildId: Snowflake) :
    ApiEndpoint.Post<Channel>("/guilds/$guildId/channels", setOf(guildId))
internal class CreateGuildRole(guildId: Snowflake) :
    ApiEndpoint.Post<Role>("/guilds/$guildId/roles", setOf(guildId))