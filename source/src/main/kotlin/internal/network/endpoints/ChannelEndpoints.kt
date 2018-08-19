package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.DmChannel
import org.http4k.core.Method

internal class GetChannel(channelId: Snowflake) : Endpoint<Channel>(Method.GET, "/channels/$channelId", channelId)

internal object CreateDmChannel : Endpoint<DmChannel>(Method.POST, "/users/@me/channels")

internal class CreateGuildChannel(guildId: Snowflake) :
    Endpoint<Channel>(Method.POST, "/guilds/$guildId/channels", guildId)
