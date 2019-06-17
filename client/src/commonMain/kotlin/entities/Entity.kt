package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.entities.MentionType.*
import com.soywiz.klock.DateTime

internal const val DISCORD_EPOCH = 1420070400000L
private const val CREATION_TIMESTAMP_BIT_DEPTH = 22

/**
 * A Discord Entity is any object with a unique identifier in the form of a 64-bit integer.
 * This unique identifier contains basic information about the entity.
 */
interface Entity {
    /**
     * A 64-bit unique identifier for a Discord entity. This ID is unique across all of Discord, except in scenarios
     * where a child entity inherits its parent's ID (such as a default channel in a guild created before the default
     * channel system was changed).
     */
    val id: Long

    /** The date and time at which this entity was created. This information is baked into the entity's ID. */
    val createdAt: DateTime get() = DateTime(DISCORD_EPOCH + (id shr CREATION_TIMESTAMP_BIT_DEPTH))
    /** The [BotClient] this [Entity] exists within. */
    val context: BotClient
}

/**
 * A [Mentionable] Entity represents any Entity which can be mentioned using
 * [Discord Mention Formatting](https://discordapp.com/developers/docs/reference#message-formatting).
 */
interface Mentionable {
    /**
     * This [Mentionable] as a formatted
     * [mention string](https://discordapp.com/developers/docs/reference#message-formatting)
     */
    val asMention: String
}

/**
 * All [Mentionable] types and a [regex] defining its mention format.
 *
 * @property regex A regex pattern that matches against this mention type.
 */
enum class MentionType(val regex: Regex) {
    /** A [User] mention (Username or Nickname). */
    USER("<@!?(\\d{1,19})>".toRegex()),
    /** A [Channel] mention. */
    CHANNEL("<#(\\d{1,19})>".toRegex()),
    /** A [GuildRole] mention. */
    ROLE("<@&(\\d{1,19})>".toRegex()),
    /** An emoji mention. */
    GUILD_EMOJI("<a?:(.{1,32}):(\\d{1,19})>".toRegex());

    /** The name of this mention type, and its associated regex pattern. */
    override fun toString(): String = "$name (regex=${regex.pattern})"
}

/** `true` if the [String] matches the given [mentionType]'s [Regex][MentionType.regex]. */
infix fun String.matches(mentionType: MentionType): Boolean = this matches mentionType.regex

/** The [MentionType] which matches this String. */
val String.mentionType: MentionType?
    get() = when {
        this matches USER -> USER
        this matches ROLE -> ROLE
        this matches CHANNEL -> CHANNEL
        this matches GUILD_EMOJI -> GUILD_EMOJI
        else -> null
    }

/** Convert a [Mentionable] ID to a mention String. */
infix fun Long.asMention(mentionType: MentionType): String = when (mentionType) {
    USER -> "<@$this>"
    ROLE -> "<@&$this>"
    CHANNEL -> "<#$this>"
    GUILD_EMOJI -> throw IllegalStateException("Cannot format GuildEmoji without name")
}
