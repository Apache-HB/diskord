package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import io.ktor.http.HttpMethod

internal class GetChannel(channelId: Long) : Endpoint<ChannelPacket>(HttpMethod.Get, "/channels/$channelId", channelId)

internal object CreateDmChannel : Endpoint<DmChannelPacket>(HttpMethod.Post, "/users/@me/channels")

internal class CreateGuildChannel(guildId: Long) :
    Endpoint<Channel>(HttpMethod.Post, "/guilds/$guildId/channels", guildId)
