package com.serebit.diskord

import com.serebit.diskord.data.ChannelData
import com.serebit.diskord.data.UserData
import com.serebit.diskord.entities.DiscordEntity
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.Channel

internal object EntityCacher {
    private val guildCache: MutableSet<Guild> = mutableSetOf()
    private val channelCache: MutableSet<Channel> = mutableSetOf()
    private val userCache: MutableSet<User> = mutableSetOf()

    fun <T : DiscordEntity> cache(entity: T): T {
        when (entity) {
            is Channel -> channelCache += entity
            is User -> userCache += entity
            is Guild -> guildCache += entity
        }

        return entity
    }

    // If the user doesn't exist in the cache, get it from Discord itself.
    fun findUser(id: Snowflake): User? =
        userCache.firstOrNull { it.id == id.toLong() } ?: requester.get<UserData>("/users/$id")?.toUser()?.also {
            userCache.add(it)
        }

    // If the channel doesn't exist in the cache, get it from Discord itself.
    fun findChannel(id: Snowflake): Channel? =
        channelCache.firstOrNull { it.id == id.toLong() } ?: requester.get<ChannelData>("/users/$id")
            ?.toChannel()
            ?.also {
                channelCache.add(it)
            }

    // If the guild doesn't exist in the cache, get it from Discord itself.
    fun findGuild(id: Snowflake) = guildCache.firstOrNull { it.id == id.toLong() }
}
