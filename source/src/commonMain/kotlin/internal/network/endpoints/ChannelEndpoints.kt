package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket
import io.ktor.http.HttpMethod

internal class GetChannel(channelId: Long) : Endpoint.Object<ChannelPacket>(
    HttpMethod.Get, "channels/$channelId", ChannelPacket.serializer(),
    channelId
)

internal class GetDmChannel(channelId: Long) : Endpoint.Object<DmChannelPacket>(
    HttpMethod.Get, "channels/$channelId", DmChannelPacket.serializer(),
    channelId
)

internal class GetGroupDmChannel(channelId: Long) : Endpoint.Object<GroupDmChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GroupDmChannelPacket.serializer(),
    channelId
)

internal class GetGuildTextChannel(channelId: Long) : Endpoint.Object<GuildTextChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GuildTextChannelPacket.serializer(),
    channelId
)

internal class GetGuildVoiceChannel(channelId: Long) : Endpoint.Object<GuildVoiceChannelPacket>(
    HttpMethod.Get, "channels/$channelId", GuildVoiceChannelPacket.serializer(),
    channelId
)

internal class GetChannelCategory(channelId: Long) : Endpoint.Object<ChannelCategoryPacket>(
    HttpMethod.Get, "channels/$channelId", ChannelCategoryPacket.serializer(),
    channelId
)

internal object CreateDmChannel : Endpoint.Object<DmChannelPacket>(
    HttpMethod.Post, "users/@me/channels", DmChannelPacket.serializer()
)

internal class CreateGuildChannel(guildId: Long) : Endpoint.Object<GuildChannelPacket>(
    HttpMethod.Post, "guilds/$guildId/channels", GuildChannelPacket.serializer(),
    guildId
)
