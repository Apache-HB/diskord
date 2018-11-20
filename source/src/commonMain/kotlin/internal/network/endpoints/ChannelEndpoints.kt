package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket
import io.ktor.http.HttpMethod

internal class GetChannel(channelId: Long) : Endpoint.ObjectData<GenericChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GenericChannelPacket.serializer(),
    channelId
)

internal class GetDmChannel(channelId: Long) : Endpoint.ObjectData<DmChannelPacket>(
    HttpMethod.Get, "channels/$channelId", DmChannelPacket.serializer(),
    channelId
)

internal class GetGroupDmChannel(channelId: Long) : Endpoint.ObjectData<GroupDmChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GroupDmChannelPacket.serializer(),
    channelId
)

internal class GetGuildTextChannel(channelId: Long) : Endpoint.ObjectData<GuildTextChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GuildTextChannelPacket.serializer(),
    channelId
)

internal class GetGuildVoiceChannel(channelId: Long) : Endpoint.ObjectData<GuildVoiceChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GuildVoiceChannelPacket.serializer(),
    channelId
)

internal class GetChannelCategory(channelId: Long) : Endpoint.ObjectData<ChannelCategoryPacket>(
    HttpMethod.Get, "channels/$channelId", ChannelCategoryPacket.serializer(),
    channelId
)

internal object CreateDmChannel : Endpoint.ObjectData<DmChannelPacket>(
    HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
)

internal class CreateGuildChannel(guildId: Long) : Endpoint.ObjectData<GenericChannelPacket>(
    HttpMethod.Post, "guilds/$guildId/channels", GenericChannelPacket.serializer(),
    guildId
)
