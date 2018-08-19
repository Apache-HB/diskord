package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Role
import org.http4k.core.Method

internal class GetGuild(guildId: Snowflake) : Endpoint<Guild>(Method.GET, "/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint<Guild>(Method.POST, "/guilds")

internal class CreateRole(guildId: Snowflake) : Endpoint<Role>(Method.POST, "/guilds/$guildId/roles", guildId)
