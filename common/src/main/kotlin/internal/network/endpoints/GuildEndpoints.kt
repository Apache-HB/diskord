package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod

internal class GetGuild(guildId: Long) : Endpoint<GuildCreatePacket>(HttpMethod.Get, "/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint<GuildCreatePacket>(HttpMethod.Post, "/guilds")

internal class CreateRole(guildId: Long) : Endpoint<RolePacket>(HttpMethod.Post, "/guilds/$guildId/roles", guildId)

internal class KickGuildMember(guildId: Long, userId: Long) :
    Endpoint<HttpResponse>(HttpMethod.Delete, "/guilds/$guildId/members/$userId", guildId)

internal class BanGuildMember(guildId: Long, userId: Long) :
    Endpoint<HttpResponse>(HttpMethod.Put, "/guilds/$guildId/bans/$userId", guildId)
