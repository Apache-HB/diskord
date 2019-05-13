package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Avatar
import com.serebit.strife.internal.entitydata.UserData
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
data class User internal constructor(private val data: UserData) : Mentionable {
    /** Reference to the [BotClient] this [User] belongs to. */
    override val context: BotClient = data.context

    override val id: Long = data.id

    override val asMention: String = "<@$id>"

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
    val username: String get() = data.username

    /**
     * The discriminator is the other half of a user's identification, and takes
     * the form of a 4-digit number. Discriminators are assigned when the user
     * is first created, and can only be changed by users with Discord Nitro.
     * No two users can share the same username/discriminator combination.
     */
    val discriminator: Int get() = data.discriminator.toInt()

    /** The display name of this [User]. It's a combination of [username] and [discriminator] (e.g. Username#0001). */
    val displayName: String get() = "$username#${discriminator.toString().padStart(4, '0')}"

    /** The [Avatar] of this [User]. */
    val avatar: Avatar get() = data.avatar

    /** `true` if this [User] is a bot. */
    val isBot: Boolean get() = data.isBot
    /** `true` if the [User] is a normal human user account. */
    val isHumanUser: Boolean get() = !isBot

    /** Creates a [DmChannel] with this [User]. */
    suspend fun createDmChannel(): DmChannel? = context.requester.sendRequest(Route.CreateDM(id)).value
        ?.toDmChannelData(context)?.toEntity()

    override fun equals(other: Any?) = other is User && other.id == id

    companion object {
        /** The minimum length that a user's [username] can have. */
        const val USERNAME_MIN_LENGTH = 2
        /** The maximum length that a user's [username] can have. */
        const val USERNAME_MAX_LENGTH = 32
        /** The range in which the length of a user's [username] must reside. */
        val USERNAME_LENGTH_RANGE = USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
    }
}
