package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.Embed.*
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.EmbedPacket
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a textual message sent in a [TextChannel]. A message can consist of text, files, and/or embeds.
 */
class Message internal constructor(private val data: MessageData) : Entity {
    override val id: Long = data.id
    override val context: BotClient = data.context

    /**
     * The [User] who sent this [Message]. If the message was sent by the system,
     * no [User] is associated with it and this property will be `null`.
     */
    suspend fun getAuthor(): User? = data.author.lazyEntity

    /** The [TextChannel] this [Message] was sent to. */
    suspend fun getChannel(): TextChannel = data.channel.lazyEntity

    /**
     * The message's text content, excluding attachments and embeds.
     * Mentions are left in [standard format](https://discordapp.com/developers/docs/reference#message-formatting).
     */
    suspend fun getContent(): String = data.content

    /** The time at which this message was last edited. If the message has never been edited, this will be null. */
    suspend fun getLastEditTime(): Instant? = data.editedAt

    /** An ordered list of [User]s that this message contains mentions for. */
    suspend fun getMentionedUsers(): List<User> = data.mentionedUsers.map { it.lazyEntity }

    /** An ordered list of [GuildRole]s that this message contains mentions for. */
    suspend fun getMentionedRoles(): List<GuildRole> = data.mentionedRoles.map { it.lazyEntity }

    /** `true` if this [Message] mentions `@everyone` */
    suspend fun mentionsEveryone(): Boolean = data.mentionsEveryoneOrHere && "@everyone" in getContent()

    /** `true` if this [Message] mentions `@here` and does not mention `@everyone` */
    suspend fun mentionsHere(): Boolean = data.mentionsEveryoneOrHere && !mentionsEveryone()

    /** `true` if this [Message] is currently pinned in the [getChannel] */
    suspend fun isPinned(): Boolean = data.isPinned

    /** `true` if the [Message] was sent as a Text-to-Speech message (`/tts`). */
    suspend fun isTextToSpeech(): Boolean = data.isTextToSpeech

    /** A [List] of all embeds in this [Message]. */
    suspend fun getEmbeds(): List<Embed> = data.embeds.map { it.toEmbed() }

    /** Edit this [Message] with the given [embed]. This can only be done when the client is the [getAuthor]. */
    suspend fun edit(embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.EditMessage(getChannel().id, id, embed = embed.build()))
            .value
            ?.toData(data.channel, context)
            ?.lazyEntity

    /**
     * Edit this [Message] with [text] and an optional [embed]. This can only be done when the client is the [getAuthor].
     */
    suspend fun edit(text: String, embed: EmbedBuilder? = null): Message? =
        context.requester.sendRequest(Route.EditMessage(getChannel().id, id, text, embed?.build()))
            .value
            ?.toData(data.channel, context)
            ?.lazyEntity

    /** Delete this [Message]. *Requires client is [getAuthor] or [Permission.ManageMessages].* */
    suspend fun delete(): Boolean =
        context.requester.sendRequest(Route.DeleteMessage(getChannel().id, id)).status.isSuccess()

    /**
     * React to this [Message] with the provided [emoji]. **Requires [Permission.ReadMessageHistory], and
     * [Permission.AddReactions] if this is the first reaction with the given [emoji] on this [Message].** Returns
     * `true` on success.
     */
    suspend fun react(emoji: Emoji): Boolean =
        context.requester.sendRequest(Route.CreateReaction(getChannel().id, id, emoji))
            .status
            .isSuccess()

    /** Delete self user's reaction with the provided [emoji], or another [user]'s reaction if a [User] was provided.
     * **Requires [Permission.ManageMessages] if deleting another user's reaction.**
     */
    suspend fun deleteReaction(emoji: Emoji, user: User? = null): Boolean = context.requester.sendRequest(
        user?.let { Route.DeleteUserReaction(getChannel().id, id, user.id, emoji) }
            ?: Route.DeleteOwnReaction(getChannel().id, id, emoji)
    ).status.isSuccess()

    /**
     * Delete all reactions on this [Message]. **Requires [Permission.ManageMessages].** Returns `true` on success.
     */
    suspend fun deleteReactions(): Boolean = context.requester.sendRequest(
        Route.DeleteAllReactions(getChannel().id, id)
    ).status.isSuccess()

    /**
     * Get a list of users who reacted on this [Message] with the provided [emoji]. [before] and [after] parameters
     * can be set to get reactions between a set of users. Additionally, you can set a [limit] between 1-100 to how
     * many users will be returned. Default limit is 25.
     *
     * Returns `null` on failure.
     */
    suspend fun getReactions(emoji: Emoji, before: User? = null, after: User? = null, limit: Int = 25): List<User>? {
        require(limit in 1..100) { "Limit must be between 1-100 (was $limit)." }

        return context.requester.sendRequest(
            Route.GetReactions(getChannel().id, id, emoji, before?.id, after?.id, limit)
        ).value?.map { it.toData(context).lazyEntity }
    }

    /** Checks if this message is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is Message && other.id == id

    /**
     * [See the entry in Discord's documentation][https://discordapp.com/developers/docs/resources/channel#message-
     * object-message-types].
     */
    enum class Type {
        /** A normal message sent by a bot or a human. */
        DEFAULT,
        /** A message that shows that a new member was added to a DM channel. */
        RECIPIENT_ADD,
        /** A message that shows that a member left a DM channel. */
        RECIPIENT_REMOVE,
        /** An informational message that notifies a user about a voice call they received. */
        CALL,
        /** An informational message that shows that a user renamed the DM channel. */
        CHANNEL_NAME_CHANGE,
        /** An informational message that shows that a user changed the icon of the DM channel. */
        CHANNEL_ICON_CHANGE,
        /** An informational message that shows that a message was pinned in the text channel. */
        CHANNEL_PINNED_MESSAGE,
        /** An informational message that shows that a user joined the [Guild]. */
        GUILD_MEMBER_JOIN,
        /** An informational message that shows when a user boosts a [Guild]. */
        USER_PREMIUM_GUILD_SUBSCRIPTION,
        /** An informational message that's triggered when a guild reaches boost tier 1. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1,
        /** An informational message that's triggered when a guild reaches boost tier 2. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2,
        /** An informational message that's triggered when a guild reaches boost tier 3. */
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3,
        /** An informational message that's triggered when a server follows another server's news channel. */
        CHANNEL_FOLLOW_ADD,
        /** An informational message that's triggered when a user starts streaming to a voice channel. */
        GUILD_STREAM
    }

    companion object {
        /** The maximum number of characters allowed in a single [Message]. */
        const val MAX_LENGTH: Int = 2000
    }
}

/** An ordered list of [TextChannel]s that this message mentions. */
suspend fun Message.mentionedChannels(): List<TextChannel> = MentionType.CHANNEL.regex.findAll(getContent()).asFlow()
    .mapNotNull { result ->
        getGuild()?.getTextChannels()?.find { it.id == result.groupValues[1].toLong() }
    }
    .toList()

/** The [Guild] this message was sent in. This is `null` if the message was sent in a [DmChannel]. */
suspend fun Message.getGuild(): Guild? = getChannel().let { if (it is GuildChannel) it.getGuild() else null }

/** The URL to this message. */
suspend fun Message.link(): String = "https://discordapp.com/channels/${getGuild()?.id ?: "@me"}/${getChannel().id}/$id"

/** Reply to this message with the given [embed]. */
suspend fun Message.reply(embed: EmbedBuilder): Message? = getChannel().send(embed)

/** Reply to this message with the given [text] and an optional [embed]. */
suspend fun Message.reply(text: String, embed: EmbedBuilder? = null): Message? = getChannel().send(text, embed)

/** Reply to this message with the given [embed]. */
suspend inline fun Message.reply(embed: EmbedBuilder.() -> Unit): Message? = getChannel().send(embed)

/** Reply to this message with the given [text] and [embed] */
suspend inline fun Message.reply(text: String, embed: EmbedBuilder.() -> Unit): Message? =
    getChannel().send(text, embed)

/** Edit this message, replacing it with the given [embed]. */
suspend inline fun Message.edit(embed: EmbedBuilder.() -> Unit): Message? = edit(EmbedBuilder().apply(embed))

/** Edit this message, replacing it with the given [text] and [embed]. */
suspend inline fun Message.edit(text: String, embed: EmbedBuilder.() -> Unit): Message? =
    edit(text, EmbedBuilder().apply(embed))

/** Returns `true` if the given [mentionable] [Entity] is mentioned in this [Message]. */
suspend infix fun Message.mentions(mentionable: Mentionable): Boolean = mentionable.asMention() in getContent()

/** Returns `true` if an [Entity] with the given [id] is mentioned in this [Message]. */
suspend infix fun Message.mentions(id: Long): Boolean = getMentionedUsers().any { it.id == id } ||
        getMentionedRoles().any { it.id == id } || mentionedChannels().any { it.id == id }

/**
 * Get a list of users who reacted on this [Message] with the provided [emoji] before the given [user]. An additional
 * [limit] between 1-100 can be set, defaults to 25.
 *
 * @see Message.getReactions
 */
suspend fun Message.getReactionsBefore(emoji: Emoji, user: User, limit: Int = 25): List<User>? =
    getReactions(emoji, before = user, limit = limit)

/**
 * Get a list of users who reacted on this [Message] with the provided [emoji] after the given [user]. An additional
 * [limit] between 1-100 can be set, defaults to 25.
 *
 * @see Message.getReactions
 */
suspend fun Message.getReactionsAfter(emoji: Emoji, user: User, limit: Int = 25): List<User>? =
    getReactions(emoji, after = user, limit = limit)

/**
 * An embed is a card-like content display sent by Webhooks and Bots. [Here](https://imgur.com/a/yOb5n) you can see
 * each part of the embed explained and shown.
 *
 * You can use an embed preview tool [like this](https://cog-creators.github.io/discord-embed-sandbox/) to see
 * what an embed might look like.
 *
 * [see official docs](https://discordapp.com/developers/docs/resources/channel#embed-object)
 *
 * @property title The title of the embed appears atop the [description] and right below the [author].
 * @property description The description of the embed appears after the [title] and before any [Field]. The
 * description supports standard Discord markdown as well as [markdown\](links).
 * @property thumbnail The thumbnail appears in the upper-right-hand corner of the embed as a smaller image. If this is
 * `null`, the embed has no thumbnail.
 * @property author The author whose name and image will appear at the top-left corner of the embed.
 * @property fields A list of all [Field]s in the embed in order of appearance (left -> right, top -> bottom).
 * @property image The large image which is shown at the bottom of the embed. If this is `null`, the embed has no image.
 * @property video The large video which is shown at the bottom of the embed. If this is `null`, the embed has no video.
 * @property color The color of the embed's left border.
 * @property footer The footer of the embed shown at the very bottom.
 * @property timestamp The timestamp is shown to the right of the [footer] and is usually used to mark when the embed
 * was sent, but can be set to any date and time.
 */
data class Embed internal constructor(
    val author: Author? = null,
    val title: Title? = null,
    val description: String? = null,
    val fields: List<Field> = emptyList(),
    val color: Color = Color.BLACK,
    val image: Graphic? = null,
    val thumbnail: Graphic? = null,
    val video: Graphic? = null,
    val footer: Footer? = null,
    val timestamp: Instant? = null
) {

    /**
     * @property text The text of the embed appears atop the [description] and right below the [author].
     * @property url The url which when the [title] is clicked will be opened. Set this to `null` for no link.
     */
    @Serializable
    data class Title internal constructor(val text: String, val url: String? = null) {
        /** Returns the [text][Title.text] of this title. */
        override fun toString(): String = text
    }

    /**
     * The Author of [Embed] shown at the top. [see](https://i.imgur.com/JgZtxIM.png)
     *
     * @property name The Author's name
     * @property url The url hyperlink of the [name]
     * @property imgUrl The url to the image/avatar
     * @property proxyImgUrl
     */
    @Serializable
    data class Author internal constructor(
        val name: String? = null, val url: String? = null, val imgUrl: String? = null, val proxyImgUrl: String? = null
    )

    /**
     * A [Field] is a titled paragraph displayed, in order, under the [description].
     *
     * @property name The title of the [Field].
     * @property value The text displayed under the [name]
     * @property inline Whether the [Field] should be displayed inline
     * (i.e., next to another inline [Field] where possible).
     */
    @Serializable
    data class Field internal constructor(val name: String, val value: String, val inline: Boolean)

    /** An image or video within the embed. */
    @Serializable
    data class Graphic internal constructor(
        /** The direct link to this [Graphic]. */
        val url: String? = null,
        /** A proxy link generated by Discord for this [Graphic] from its [url]. */
        val proxyUrl: String? = null,
        /** The height of this [Graphic], determined by Discord. */
        val height: Short? = null,
        /** The width of this [Graphic], determined by Discord. */
        val width: Short? = null
    )

    /**
     * The footer of the [Embed]. [see](https://i.imgur.com/jdf4sbi.png).
     */
    @Serializable
    data class Footer internal constructor(
        /** The text of the footer. */
        val text: String?,
        /** The direct link to this footer's icon. */
        val iconUrl: String? = null,
        /** A proxy link generated by Discord for this footer's icon from its [iconUrl]. */
        val proxyIconUrl: String? = null
    )

}

/** Convert an [EmbedPacket] to a user-facing [Embed]. */
internal fun EmbedPacket.toEmbed() = Embed(
    author?.let { Author(it.name, it.url, it.icon_url, it.proxy_icon_url) },
    title?.let { Title(it, this@toEmbed.url) },
    description,
    fields?.let { list -> list.map { f -> Field(f.name, f.value, f.inline ?: false) } } ?: emptyList(),
    color?.let { Color(it) } ?: Color.GREYPLE,
    image?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    thumbnail?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    video?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    footer?.let { Footer(it.text, it.icon_url, it.proxy_icon_url) },
    timestamp?.let { Instant.parse(it) }
)
