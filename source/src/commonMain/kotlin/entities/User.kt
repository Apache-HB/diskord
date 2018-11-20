package com.serebit.diskord.entities

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.endpoints.GetUser

/**
 * Users in Discord are generally considered the base entity. Users can spawn across the entire platform, be members of
 * guilds, participate in text and voice chat, and much more. Users are separated by a distinction of "bot" vs "normal."
 * Although they are similar, bot users are automated users that are "owned" by another user. Unlike normal users, bot
 * users do not have a limitation on the number of Guilds they can be a part of.
 */
data class User internal constructor(override val id: Long, override val context: Context) : Entity {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: context.requester.requestObject(GetUser(id))
            ?: throw EntityNotFoundException("Invalid user instantiated with ID $id.")
    /**
     * The username represents the most basic form of identification for any Discord user. Usernames are not unique
     * across Discord, and as such, several users can share the same username. However, no two users can share the
     * same username/discriminator combination.
     *
     * There are a few Discord-enforced restrictions for usernames, and they are as follows:
     *
     * - Names can contain most valid unicode characters, minus some zero-width and non-rendering characters.
     * - Names must be between 2 and 32 characters long.
     * - Names are sanitized and trimmed of leading, trailing, and excessive internal whitespace.
     * - Names cannot contain the following substrings: '@', '#', ':', '```'.
     * - Names cannot be: 'discordtag', 'everyone', 'here'.
     */
    val username: String get() = packet.username
    /**
     * The discriminator is the other half of a user's identification, and takes the form of a 4-digit number.
     * Discriminators are assigned when the user is first created, and can only be changed by users with Discord
     * Nitro. No two users can share the same username/discriminator combination.
     */
    val discriminator: Int get() = packet.discriminator
    val avatar get() = packet.avatarObj
    val isBot: Boolean get() = packet.isBot
    val isNormalUser: Boolean get() = !isBot
    val hasMfaEnabled: Boolean? get() = packet.mfa_enabled
    val isVerified: Boolean? get() = packet.verified

    companion object {
        const val USERNAME_MIN_LENGTH = 2
        const val USERNAME_MAX_LENGTH = 32
        val USERNAME_LENGTH_RANGE = USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
    }
}
