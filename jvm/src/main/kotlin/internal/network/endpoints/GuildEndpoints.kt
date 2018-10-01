package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import org.http4k.core.Method
import org.http4k.core.Response

internal class GetGuild(guildId: Long) : Endpoint<GuildCreatePacket>(Method.GET, "/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint<GuildCreatePacket>(Method.POST, "/guilds")

internal class CreateRole(guildId: Long) : Endpoint<RolePacket>(Method.POST, "/guilds/$guildId/roles", guildId)

internal class KickGuildMember(guildId: Long, userId: Long) :
    Endpoint<Response>(Method.DELETE, "/guilds/$guildId/members/$userId", guildId)

internal class BanGuildMember(guildId: Long, userId: Long) :
    Endpoint<Response>(Method.PUT, "/guilds/$guildId/bans/$userId", guildId)
