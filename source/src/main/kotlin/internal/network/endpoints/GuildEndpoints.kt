package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.RolePacket
import org.http4k.core.Method

internal class GetGuild(guildId: Long) : Endpoint<GuildCreatePacket>(Method.GET, "/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint<GuildCreatePacket>(Method.POST, "/guilds")

internal class CreateRole(guildId: Long) : Endpoint<RolePacket>(Method.POST, "/guilds/$guildId/roles", guildId)
