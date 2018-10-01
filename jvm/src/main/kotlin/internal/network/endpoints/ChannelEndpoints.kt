package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import org.http4k.core.Method

internal class GetChannel(channelId: Long) : Endpoint<ChannelPacket>(Method.GET, "/channels/$channelId", channelId)

internal object CreateDmChannel : Endpoint<DmChannelPacket>(Method.POST, "/users/@me/channels")

internal class CreateGuildChannel(guildId: Long) :
    Endpoint<Channel>(Method.POST, "/guilds/$guildId/channels", guildId)