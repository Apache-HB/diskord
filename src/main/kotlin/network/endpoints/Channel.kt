@file:JvmName("ChannelEndpoints")

package com.serebit.diskord.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.DmChannel

internal class GetChannel(channelId: Snowflake) : Endpoint.Get<Channel>("/channels/$channelId", channelId)
internal object CreateDmChannel : Endpoint.Post<DmChannel>("/users/@me/channels")
internal class CreateGuildChannel(guildId: Snowflake) : Endpoint.Post<Channel>("/guilds/$guildId/channels", guildId)
