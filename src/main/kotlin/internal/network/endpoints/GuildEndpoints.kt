package com.serebit.diskord.internal.network.endpoints

import com.serebit.diskord.Snowflake
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Role

internal class GetGuild(guildId: Snowflake) : Endpoint.Get<Guild>("/guilds/$guildId", guildId)

internal object CreateGuild : Endpoint.Post<Guild>("/guilds")

internal class CreateRole(guildId: Snowflake) : Endpoint.Post<Role>("/guilds/$guildId/roles", guildId)
