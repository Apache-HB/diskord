package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.AvatarData
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.internal.entitydata.GuildChannelCategoryData
import com.serebit.strife.internal.entitydata.GuildNewsChannelData
import com.serebit.strife.internal.entitydata.GuildStoreChannelData
import com.serebit.strife.internal.entitydata.GuildVoiceChannelData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.toInvite
import com.soywiz.klock.DateTimeTz
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow

/** Represents a text or voice channel within Discord. */
interface Channel : Entity {
    /**
     * The types of channels which bot clients can interact with.
     * @property id The ID int of the channel type,
     * [see](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types)
     */
    enum class Type(val id: Int) {
        /** [GuildTextChannel] */
        GUILD_TEXT(0),
        /** [DmChannel] */
        DM(1),
        /** [GuildVoiceChannel] */
        GUILD_VOICE(2),
        /** [GuildChannelCategory] */
        GUILD_CATEGORY(4),
        /** [GuildNewsChannel] */
        GUILD_NEWS(5),
        /** [GuildStoreChannel] */
        GUILD_STORE(6)
    }
}

/** A [Channel] used to send textual messages with optional attachments. */
interface TextChannel : Channel {
    /** The last message sent in this channel. */
    suspend fun lastMessage(): Message?

    /** The date and time of the last time a message was pinned in this [TextChannel]. */
    suspend fun lastPinTime(): DateTimeTz?

    /** Send an [Embed][EmbedBuilder] to this [TextChannel]. Returns the sent [Message] or null if not sent. */
    suspend fun send(embed: EmbedBuilder): Message?

    /**
     * Send a [Message] with [text] and an optional [embed] to this [TextChannel].
     * Returns the [Message] which was sent or null if it was not sent.
     */
    suspend fun send(text: String, embed: EmbedBuilder? = null): Message?

    /** Show the bot client as `bot_name is typing...` beneath the text-entry box. Returns `true` if successful. */
    suspend fun sendTyping(): Boolean =
        context.requester.sendRequest(Route.TriggerTypingIndicator(id)).status.isSuccess()

    /**
     * Returns a flow of this channel's [Message]s with an optional [limit] and either [before] or [after]
     * @param before The message id to get messages before.
     * @param after The message id to get messages after.
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfMessages(before: Long? = null, after: Long? = null, limit: Int? = null): Flow<Message>

    /**
     * Returns a flow of this channel's [Message]s [before] a given [Message] with an optional [limit]
     * @param before The message id to get messages before.
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfMessagesBefore(before: Long?, limit: Int? = null): Flow<Message> =
        flowOfMessages(before = before, limit = limit)

    /**
     * Returns a flow of this channel's [Message]s [after] a given [Message] with an optional [limit]
     * @param after The message id to get messages after.
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfMessagesAfter(after: Long, limit: Int? = null): Flow<Message> =
        flowOfMessages(after = after, limit = limit)

    /**
     * Returns the channel's history as a flow of [Message]s with an optional [limit]
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfHistory(limit: Int? = null): Flow<Message> = flowOfMessagesBefore(lastMessage()?.id, limit)

    /**
     * Returns the channel's history as a flow of [Message]s from start with an optional [limit]
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfHistoryFromStart(limit: Int? = null): Flow<Message> = flowOfMessagesAfter(0, limit)
}

/** A Private Direct Message [TextChannel] used to talk with a single [User]. */
class DmChannel internal constructor(override val id: Long, override val context: BotClient) : TextChannel {
    private suspend fun getData() = context.obtainDmChannelData(id)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent DM channel with ID $id")

    override suspend fun lastMessage(): Message? = getData().lastMessage?.lazyEntity

    override suspend fun lastPinTime(): DateTimeTz? = getData().lastPinTime

    /** The [users][User] who have access to this [DmChannel]. */
    suspend fun recipient(): User? = getData().recipient?.lazyEntity

    override suspend fun send(embed: EmbedBuilder): Message? = getData().send(embed = embed)?.lazyEntity

    override suspend fun send(text: String, embed: EmbedBuilder?): Message? = getData().send(text, embed)?.lazyEntity

    override suspend fun flowOfMessages(before: Long?, after: Long?, limit: Int?): Flow<Message> =
        getData().flowOfMessages(before, after, limit)

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is Entity && other.id == id
}

/**  A representation of any [Channel] which can only be found within a [Guild]. */
interface GuildChannel : Channel {
    /** The [Guild] housing this channel. */
    suspend fun guild(): Guild

    /** The sorting position of this channel in its [guild]. */
    suspend fun position(): Int

    /** The displayed name of this channel in its [guild]. */
    suspend fun name(): String

    /** Explicit [permission overrides][PermissionOverride] for members and roles. */
    suspend fun permissionOverrides(): Map<Long, PermissionOverride>

    /**
     * Create a new [Invite] for this [GuildChannel]. You can optionally specify details about the invite like
     * the [maximum age in seconds][ageLimit], the [maximum number of uses][useLimit], whether to grant [temporary]
     * membership, and whether this Invite must be [unique] (useful for creating many unique one time use invites).
     *
     * If an [Invite] is set to grant [temporary] membership, users will be removed from the [guild] when they
     * disconnect -- unless they have been assigned a [GuildRole].
     *
     * Returns the code of the newly created invite or `null` if one was not created.
     */
    suspend fun createInvite(
        ageLimit: Int = 86400,
        useLimit: Int = 0,
        temporary: Boolean = false,
        unique: Boolean = false
    ): String? = context.requester.sendRequest(
        Route.CreateChannelInvite(id, ageLimit, useLimit, temporary, unique)
    ).value?.code

    /** Returns a list of [Invite]s associated with this [GuildChannel] or `null` if the request failed. */
    suspend fun getInvites(): List<Invite>? = context.requester.sendRequest(Route.GetChannelInvites(id)).value
        ?.map { ip -> ip.toInvite(context, guild(), guild().members.firstOrNull { it.user.id == ip.inviter.id }) }

    /** Returns the [Invite] with the given [code]. Returns `null` if the request fails or no [Invite] is found. */
    suspend fun getInvite(code: String): Invite? = getInvites()?.firstOrNull { it.code == code }
}

interface GuildMessageChannel : TextChannel, GuildChannel {
    /** The topic displayed above the message window and next to the channel name (0-1024 characters). */
    suspend fun topic(): String

    /**
     * Whether this channel is marked as NSFW. NSFW channels have two main differences: users have to explicitly say
     * that they are willing to view potentially unsafe-for-work content via a prompt, and these channels are exempt
     * from [explicit content filtering][Guild.explicitContentFilter].
     */
    suspend fun isNsfw(): Boolean

    /** Get all [webhooks][Webhook] of this channel. Returns a [List] of [Webhook], or `null` on failure. */
    suspend fun getWebhooks(): List<Webhook>?

    /**
     * Create a [Webhook] in this channel with the given [name], and optionally an [avatar]. Returns the created
     * [Webhook], or `null` on failure.
     */
    suspend fun createWebhook(name: String, avatar: AvatarData? = null): Webhook?
}

/** A [TextChannel] found within a [Guild]. */
class GuildTextChannel internal constructor(override val id: Long, override val context: BotClient) :
    GuildMessageChannel, Mentionable {

    private suspend fun getData() = context.obtainGuildTextChannelData(id)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent guild text channel with ID $id")

    override suspend fun asMention(): String = id.asMention(MentionType.CHANNEL)
    override suspend fun name(): String = getData().name
    override suspend fun guild(): Guild = getData().guild.lazyEntity
    override suspend fun position(): Int = getData().position.toInt()
    override suspend fun permissionOverrides(): Map<Long, PermissionOverride> = getData().permissionOverrides
    override suspend fun lastMessage(): Message? = getData().lastMessage?.lazyEntity
    override suspend fun lastPinTime(): DateTimeTz? = getData().lastPinTime
    override suspend fun topic(): String = getData().topic
    override suspend fun isNsfw(): Boolean = getData().isNsfw
    /** A configurable per-user rate limit that defines how often a user can send messages in this channel. */
    suspend fun getRateLimitPerUser(): Int? = getData().rateLimitPerUser?.toInt()

    override suspend fun send(embed: EmbedBuilder): Message? = getData().send(embed = embed)?.lazyEntity

    override suspend fun send(text: String, embed: EmbedBuilder?): Message? = getData().send(text, embed)?.lazyEntity

    override suspend fun flowOfMessages(before: Long?, after: Long?, limit: Int?): Flow<Message> =
        getData().flowOfMessages(before, after, limit)

    override suspend fun getWebhooks(): List<Webhook>? = context.requester.sendRequest(Route.GetChannelWebhooks(id))
        .value
        ?.map { it.toEntity(context, getData().guild, getData()) }

    override suspend fun createWebhook(name: String, avatar: AvatarData?): Webhook? = context.requester
        .sendRequest(Route.CreateWebhook(id, name, avatar))
        .value
        ?.toEntity(context, getData().guild, getData())

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildTextChannel && other.id == id
}

/**
 * A channel that users can follow and crosspost into their own server.
 * News channels can be interacted with the same way [GuildTextChannel] can be.
 * News channels are only available to some verified guilds "for now" - Discord Devs.
 */
class GuildNewsChannel internal constructor(override val id: Long, override val context: BotClient) :
    GuildMessageChannel {

    private suspend fun getData() = (context.obtainGuildChannelData(id) as? GuildNewsChannelData)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent guild news channel with ID $id")

    override suspend fun name(): String = getData().name
    override suspend fun guild(): Guild = getData().guild.lazyEntity
    override suspend fun position(): Int = getData().position.toInt()
    override suspend fun permissionOverrides(): Map<Long, PermissionOverride> = getData().permissionOverrides
    override suspend fun lastMessage(): Message? = getData().lastMessage?.lazyEntity
    override suspend fun lastPinTime(): DateTimeTz? = getData().lastPinTime
    override suspend fun topic(): String = getData().topic
    override suspend fun isNsfw(): Boolean = getData().isNsfw

    override suspend fun send(embed: EmbedBuilder): Message? = getData().send(embed = embed)?.lazyEntity

    override suspend fun send(text: String, embed: EmbedBuilder?): Message? = getData().send(text, embed)?.lazyEntity

    override suspend fun flowOfMessages(before: Long?, after: Long?, limit: Int?): Flow<Message> =
        getData().flowOfMessages(before, after, limit)

    override suspend fun getWebhooks(): List<Webhook>? = context.requester.sendRequest(Route.GetChannelWebhooks(id))
        .value
        ?.map { it.toEntity(context, getData().guild, getData()) }

    override suspend fun createWebhook(name: String, avatar: AvatarData?): Webhook? = context.requester
        .sendRequest(Route.CreateWebhook(id, name, avatar))
        .value
        ?.toEntity(context, getData().guild, getData())

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildNewsChannel && other.id == id
}

/** A channel in which game developers can sell their game on Discord. */
class GuildStoreChannel internal constructor(override val id: Long, override val context: BotClient) : GuildChannel,
    Mentionable {

    private suspend fun getData() = (context.obtainGuildChannelData(id) as? GuildStoreChannelData)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent guild store channel with ID $id")

    override suspend fun asMention(): String = id.asMention(MentionType.CHANNEL)
    override suspend fun name(): String = getData().name
    override suspend fun position(): Int = getData().position.toInt()
    override suspend fun guild(): Guild = getData().guild.lazyEntity
    override suspend fun permissionOverrides(): Map<Long, PermissionOverride> = getData().permissionOverrides

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildStoreChannel && other.id == id
}

/** A Voice Channel (which is found within a [Guild]). */
class GuildVoiceChannel internal constructor(override val id: Long, override val context: BotClient) : GuildChannel {
    private suspend fun getData() = (context.obtainGuildChannelData(id) as? GuildVoiceChannelData)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent guild voice channel with ID $id")

    override suspend fun name(): String = getData().name
    override suspend fun position(): Int = getData().position.toInt()
    override suspend fun guild(): Guild = getData().guild.lazyEntity
    override suspend fun permissionOverrides(): Map<Long, PermissionOverride> = getData().permissionOverrides
    /**
     * The bitrate of the [GuildVoiceChannel] from 8 Kbps` to `96 Kbps`; basically how much data should the channel try
     * to send when people speak ([read this for more information](https://techterms.com/definition/bitrate)).
     * Going above 64 Kbps will negatively affect users on mobile or with poor connection.
     */
    suspend fun bitrate(): Int = getData().bitrate

    /**
     * The maximum number of [users][User] allowed in the [VoiceChannel][GuildVoiceChannel] at the same time.
     * The limit can be in the range `1..99`, if set to `0` there is no limit.
     */
    suspend fun userLimit(): Int = getData().userLimit.toInt()

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildVoiceChannel && other.id == id
}

/** A collapsible channel category (which is found within a [Guild]). */
class GuildChannelCategory internal constructor(override val id: Long, override val context: BotClient) : GuildChannel {
    private suspend fun getData() = (context.obtainGuildChannelData(id) as? GuildChannelCategoryData)
        ?: throw IllegalStateException("Attempted to get data for a nonexistent guild voice channel with ID $id")

    override suspend fun name(): String = getData().name
    override suspend fun guild(): Guild = getData().guild.lazyEntity
    override suspend fun position(): Int = getData().position.toInt()
    override suspend fun permissionOverrides(): Map<Long, PermissionOverride> = getData().permissionOverrides

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildChannelCategory && other.id == id
}

/** Build and Send an [Embed] to the [TextChannel]. Returns the [Message] which was sent or null if it was not sent. */
suspend inline fun TextChannel.send(embed: EmbedBuilder.() -> Unit): Message? = send(EmbedBuilder().apply(embed))

/**
 * Build and Send an [Embed] to the [TextChannel] with additional [text].
 * Returns the [Message] which was sent or null if it was not sent.
 */
suspend inline fun TextChannel.send(text: String, embed: EmbedBuilder.() -> Unit): Message? =
    send(text, EmbedBuilder().apply(embed))
