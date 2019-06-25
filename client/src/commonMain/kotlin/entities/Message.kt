package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.Color
import com.serebit.strife.data.Permission
import com.serebit.strife.entities.Embed.*
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.EmbedPacket
import com.serebit.strife.internal.packets.GetReactionsPacket
import com.serebit.strife.internal.packets.MessageEditPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess
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
    val author: User? get() = data.author.lazyEntity

    /** The [TextChannel] this [Message] was sent to. */
    val channel: TextChannel get() = data.channel.lazyEntity

    /** The [Guild] this message was sent in. This is `null` if the message was sent in a [DmChannel]. */
    val guild: Guild? get() = data.guild?.lazyEntity

    /**
     * The message's text content, excluding attachments and embeds.
     * Mentions are left in [standard format](https://discordapp.com/developers/docs/reference#message-formatting).
     */
    val content: String get() = data.content

    /** The message's text content as it appears on the Discord client. */
    val displayContent: String
        get() = data.content
            .replace(MentionType.USER.regex) { result ->
                data.guild?.getMemberData(result.groupValues[1].toLong())
                    ?.let { it.nickname ?: it.user.username }
                    ?.let { "@$it" }
                    ?: result.value
            }.replace(MentionType.CHANNEL.regex) { result ->
                guild?.textChannels?.firstOrNull { it.id == result.groupValues[1].toLong() }
                    ?.let { "#${it.name}" }
                    ?: result.value
            }.replace(MentionType.ROLE.regex) { result ->
                guild?.roles?.firstOrNull { it.id == result.groupValues[1].toLong() }
                    ?.let { "@${it.name}" }
                    ?: result.value
            }.replace(MentionType.GUILD_EMOJI.regex) { it.groupValues[1].let { ":$it:" } }

    /** The time at which this message was last edited. If the message has never been edited, this will be null. */
    val editedAt: DateTimeTz? get() = data.editedAt

    /** An ordered list of [User]s that this message contains mentions for. */
    val mentionedUsers: List<User> get() = data.mentionedUsers.map { it.lazyEntity }

    /** An ordered list of [GuildRole]s that this message contains mentions for. */
    val mentionedRoles: List<GuildRole> get() = data.mentionedRoles.map { it.lazyEntity }

    /** An ordered list of [TextChannel]s that this message mentions. */
    val mentionedChannels: List<TextChannel>
        get() = MentionType.CHANNEL.regex.findAll(content)
            .mapNotNull { result ->
                guild?.textChannels?.firstOrNull {
                    it.id == result.groupValues[1].toLong()
                }
            }
            .toList()

    /** `true` if this [Message] mentions `@everyone` */
    val mentionsEveryone: Boolean get() = data.mentionsEveryone && "@everyone" in content

    /** `true` if this [Message] mentions `@here` and does not mention `@everyone` */
    val mentionsHere: Boolean get() = data.mentionsEveryone && !mentionsEveryone

    /** `true` if this [Message] is currently pinned in the [channel] */
    val isPinned: Boolean get() = data.isPinned

    /** `true` if the [Message] was sent as a Text-to-Speech message (`/tts`). */
    val isTextToSpeech get() = data.isTextToSpeech

    /** The URL to this message. */
    val link: String get() = "https://discordapp.com/channels/${guild?.id ?: "@me"}/${channel.id}/$id"

    /** A [List] of all embeds in this [Message]. */
    val embeds: List<Embed> get() = data.embeds.map { it.toEmbed() }

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(text: String): Message? {
        require(text.length in 1..MAX_LENGTH)
        return context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(text)))
            .value
            ?.toData(context)
            ?.lazyEntity
    }

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(embed = embed.build())))
            .value
            ?.toData(context)
            ?.lazyEntity

    /** Edit this [Message]. This can only be done when the client is the [author]. */
    suspend fun edit(text: String, embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.EditMessage(channel.id, id, MessageEditPacket(text, embed.build())))
            .value
            ?.toData(context)
            ?.lazyEntity

    /** Delete this [Message]. *Requires client is [author] or [Permission.ManageMessages].* */
    suspend fun delete(): Boolean =
        context.requester.sendRequest(Route.DeleteMessage(channel.id, id)).status.isSuccess()

    /**
     * React to this [Message] with the provided [emoji]. **Requires [Permission.ReadMessageHistory], and
     * [Permission.AddReactions] if this is the first reaction with the given [emoji] on this [Message].** Returns
     * `true` on success.
     */
    suspend fun react(emoji: Emoji): Boolean =
        context.requester.sendRequest(Route.CreateReaction(channel.id, id, emoji)).status.isSuccess()

    /** Delete self user's reaction with the provided [emoji], or another [user]'s reaction if a [User] was provided.
     * **Requires [Permission.ManageMessages] if deleting another user's reaction.**
     */
    suspend fun deleteReaction(emoji: Emoji, user: User? = null): Boolean = context.requester.sendRequest(
        user?.let { Route.DeleteUserReaction(channel.id, id, emoji, user.id) }
            ?: Route.DeleteOwnReaction(channel.id, id, emoji)
    ).status.isSuccess()

    /**
     * Delete all reactions on this [Message]. **Requires [Permission.ManageMessages].** Returns `true` on success.
     */
    suspend fun deleteReactions(): Boolean = context.requester.sendRequest(
        Route.DeleteAllReactions(channel.id, id)
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
            Route.GetReactions(channel.id, id, emoji, GetReactionsPacket(before?.id, after?.id, limit))
        ).value?.map { it.toData(context).lazyEntity }
    }

    /** Returns the [content] of this message. */
    override fun toString(): String = content

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
        GUILD_MEMBER_JOIN
    }

    companion object {
        /** The maximum number of characters allowed in a single [Message]. */
        const val MAX_LENGTH: Int = 2000
    }
}

/** Reply to this message with the given [text]. */
suspend fun Message.reply(text: String): Message? = channel.send(text)

/** Reply to this message with the given [embed]. */
suspend fun Message.reply(embed: EmbedBuilder): Message? = channel.send(embed)

/** Reply to this message with the given [text] and [embed]. */
suspend fun Message.reply(text: String, embed: EmbedBuilder): Message? = channel.send(text, embed)

/** Reply to this message with the given [embed]. */
suspend fun Message.reply(embed: EmbedBuilder.() -> Unit): Message? = channel.send(embed)

/** Reply to this message with the given [text] and [embed] */
suspend fun Message.reply(text: String, embed: EmbedBuilder.() -> Unit): Message? = channel.send(text, embed)

/** Edit this message, replacing it with the given [embed]. */
suspend inline fun Message.edit(embed: EmbedBuilder.() -> Unit): Message? = edit(EmbedBuilder().apply(embed))

/** Edit this message, replacing it with the given [text] and [embed]. */
suspend inline fun Message.edit(text: String, embed: EmbedBuilder.() -> Unit): Message? =
    edit(text, EmbedBuilder().apply(embed))

/** Returns `true` if the given [text] is in this message's content. */
operator fun Message.contains(text: String): Boolean = text in content

/** Returns `true` if the given [mentionable] [Entity] is mentioned in this [Message]. */
infix fun Message.mentions(mentionable: Mentionable): Boolean = mentionable.asMention in this

/** Returns `true` if an [Entity] with the given [id] is mentioned in this [Message]. */
infix fun Message.mentions(id: Long): Boolean = mentionedUsers.any { it.id == id } ||
        mentionedRoles.any { it.id == id } || mentionedChannels.any { it.id == id }

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
    val timestamp: DateTime? = null
) {

    /**
     * @property text The text of the embed appears atop the [description] and right below the [author].
     * @property url The url which when the [title] is clicked will be opened. Set this to `null` for no link.
     */
    @Serializable
    data class Title internal constructor(val text: String, val url: String? = null) {
        /** Returns the [text] of this title. */
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
    color?.let { Color(it) } ?: Color.BLACK, // TODO Default discord grey? https://discordapp.com/branding
    image?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    thumbnail?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    video?.let { Graphic(it.url, it.proxy_url, it.height, it.width) },
    footer?.let { Footer(it.text, it.icon_url, it.proxy_icon_url) },
    timestamp?.let { DateFormat.ISO_WITH_MS.tryParse(it)?.local }
)
