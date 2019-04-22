package com.serebit.strife.entities

import com.serebit.strife.data.Avatar
import com.serebit.strife.internal.entitydata.UserData

/**
 * Users in Discord are generally considered the base entity. Users can spawn across the entire platform, be members
 * of guilds, participate in text and voice chat, and much more. Users are separated by a distinction of "bot" vs
 * "normal." Although they are similar, bot users are automated users that are "owned" by another user. Unlike normal
 * users, bot users do not have a limitation on the number of Guilds they can be a part of.
 */
data class User internal constructor(private val data: UserData) : Entity {
    override val id = data.id
    override val context = data.context
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
    /** The user's [Avatar] (profile picture). */
    val avatar get() = data.avatar
    /** `true` if the user belongs to an OAuth2 application (i.e. is a Bot account) */
    val isBot: Boolean get() = data.isBot
    /** `true` if the [User] is a normal human user account. */
    val isHumanUser: Boolean get() = !isBot
    /** `true` if the user has two factor enabled on their account. */
    val hasMfaEnabled: Boolean? get() = data.hasMfaEnabled
    /** `true` if the email on this account has been verified. */
    val isVerified: Boolean? get() = data.isVerified

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
