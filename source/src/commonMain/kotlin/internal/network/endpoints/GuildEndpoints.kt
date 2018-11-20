package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import io.ktor.http.HttpMethod

internal class GetGuild(guildId: Long) : Endpoint.ObjectData<GuildCreatePacket>(
    HttpMethod.Get, "/guilds/$guildId", GuildCreatePacket.serializer(),
    guildId
)

internal object CreateGuild : Endpoint.ObjectData<GuildCreatePacket>(
    HttpMethod.Post, "/guilds", GuildCreatePacket.serializer()
)

internal class CreateRole(guildId: Long) : Endpoint.ObjectData<RolePacket>(
    HttpMethod.Post, "guilds/$guildId/roles", RolePacket.serializer(),
    guildId
)

internal class KickGuildMember(guildId: Long, userId: Long) : Endpoint.NoData(
    HttpMethod.Delete, "guilds/$guildId/members/$userId",
    guildId
)

internal class BanGuildMember(guildId: Long, userId: Long) : Endpoint.NoData(
    HttpMethod.Put, "guilds/$guildId/bans/$userId",
    guildId
)
