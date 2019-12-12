package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.internal.entitydata.toDmChannelData
import com.serebit.strife.internal.network.Route

/**
 * Users in Discord are generally considered the base entity. Users can spawn across the entire platform, be members
 * of guilds, participate in text and voice chat, and much more.
 *
 * Users are separated by a distinction of "bot" vs "normal." Although they are similar, bot users are automated users
 * that are "owned" by another user. Unlike normal users, bot users do not have a limitation on the number of Guilds
 * they can be a part of.
 */
class User internal constructor(override val id: Long, override val context: BotClient) : Entity, Mentionable {
    private suspend fun getData() = context.obtainUserData(id)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent user with ID $id")

    override suspend fun asMention(): String = "<@$id>"

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
    suspend fun getUsername(): String = getData().username

    /**
     * The discriminator is the other half of a user's identification, and takes the form of a 4-digit number.
     * Discriminators are assigned when the user is first created, and can only be changed by users with Discord Nitro.
     * No two users can share the same username/discriminator combination.
     */
    suspend fun getDiscriminator(): Int = getData().discriminator.toInt()

    /** The [Avatar] of this [User]. */
    suspend fun getAvatar(): Avatar = getData().avatar

    /** `true` if this [User] is a bot. */
    suspend fun isBot(): Boolean = getData().isBot

    /** Creates a [DmChannel] with this [User]. */
    suspend fun createDmChannel(): DmChannel? = context.requester.sendRequest(Route.CreateDM(id)).value
        ?.toDmChannelData(context)?.lazyEntity

    /** Checks if this user is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is User && other.id == id

    companion object {
        /** The minimum length that a user's [getUsername] can have. */
        const val USERNAME_MIN_LENGTH: Int = 2
        /** The maximum length that a user's [getUsername] can have. */
        const val USERNAME_MAX_LENGTH: Int = 32
        /** The range in which the length of a user's [getUsername] must reside. */
        val USERNAME_LENGTH_RANGE: IntRange = USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
    }
}

/** The display name of this [User]. It's a combination of [username] and [discriminator] (e.g. Username#0001). */
suspend fun User.getDisplayName(): String =
    "${getUsername()}#${getDiscriminator().toString().padStart(4, '0')}"

/** `true` if the [User] is a normal human user account. */
suspend fun User.isHuman(): Boolean = !isBot()
