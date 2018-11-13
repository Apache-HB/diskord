package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import io.ktor.http.HttpMethod

internal class GetChannel(channelId: Long) :
    Endpoint.Object<ChannelPacket>(HttpMethod.Get, "channels/$channelId", channelId)

internal class GetDmChannel(channelId: Long) :
    Endpoint.Object<DmChannelPacket>(HttpMethod.Get, "channels/$channelId", channelId)

internal object CreateDmChannel :
    Endpoint.Object<DmChannelPacket>(HttpMethod.Post, "users/@me/channels")

internal class CreateGuildChannel(guildId: Long) :
    Endpoint.Object<ChannelPacket>(HttpMethod.Post, "guilds/$guildId/channels", guildId)
