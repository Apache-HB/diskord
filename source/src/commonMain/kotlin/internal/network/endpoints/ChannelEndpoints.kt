package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import io.ktor.http.HttpMethod

internal class GetChannel(channelId: Long) : Endpoint.Object<ChannelPacket>(
    HttpMethod.Get, "channels/$channelId", ChannelPacket.serializer(),
    channelId
)

internal class GetDmChannel(channelId: Long) : Endpoint.Object<DmChannelPacket>(
    HttpMethod.Get, "channels/$channelId", DmChannelPacket.serializer(),
    channelId
)

internal object CreateDmChannel : Endpoint.Object<DmChannelPacket>(
    HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
)

internal class CreateGuildChannel(guildId: Long) : Endpoint.Object<GuildChannelPacket>(
    HttpMethod.Post, "guilds/$guildId/channels", GuildChannelPacket.serializer(),
    guildId
)
