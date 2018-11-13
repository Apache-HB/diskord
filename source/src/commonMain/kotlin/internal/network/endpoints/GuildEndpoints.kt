package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import io.ktor.http.HttpMethod

internal class GetGuild(guildId: Long) : Endpoint.Object<GuildCreatePacket>(
    HttpMethod.Get, "/guilds/$guildId",
    guildId
)

internal object CreateGuild : Endpoint.Object<GuildCreatePacket>(HttpMethod.Post, "/guilds")

internal class CreateRole(guildId: Long) :
    Endpoint.Object<RolePacket>(HttpMethod.Post, "guilds/$guildId/roles", guildId)

internal class KickGuildMember(guildId: Long, userId: Long) : Endpoint.Response(
    HttpMethod.Delete, "guilds/$guildId/members/$userId",
    guildId
)

internal class BanGuildMember(guildId: Long, userId: Long) : Endpoint.Response(
    HttpMethod.Put, "guilds/$guildId/bans/$userId",
    guildId
)
