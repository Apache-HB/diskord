package com.serebit.diskord.entities

import com.serebit.diskord.EntityCache
import com.serebit.diskord.Snowflake
import com.serebit.diskord.data.Permission
import com.serebit.diskord.packets.GuildPacket

class Guild internal constructor(data: GuildPacket) : Entity {
    override val id: Snowflake = data.id
    var owner: User = data.members.map { it.user }.first { it.id == data.owner_id }
    var permissions = Permission.from(data.permissions ?: 0)

    init {
        EntityCache.cache(this)
    }
}
