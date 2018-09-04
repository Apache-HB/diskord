package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.RolePacket
import org.http4k.core.Method

internal class GetGuild(guildId: Long) : Endpoint<GuildPacket>(Method.GET, "/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint<GuildPacket>(Method.POST, "/guilds")

internal class CreateRole(guildId: Long) : Endpoint<RolePacket>(Method.POST, "/guilds/$guildId/roles", guildId)
