package com.serebit.strife.entities

import com.serebit.strife.BotClient
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.MessageSendPacket
import com.soywiz.klock.DateTimeTz

/** Represents a text or voice channel within Discord. */
interface Channel : Entity

/** A [Channel] used to send textual messages with optional attachments. */
interface TextChannel : Channel {
    /** The last message sent in this channel. */
    val lastMessage: Message?
    /** The date and time of the last time a message was pinned in this [TextChannel]. */
    val lastPinTime: DateTimeTz?

    /** Send a [Message] to this [TextChannel]. Returns the [Message] which was sent or null if it was not sent. */
    suspend fun send(text: String): Message? {
        require(text.length in 1..Message.MAX_LENGTH)
        return context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(text)))
            .value
            ?.toData(context)
            ?.lazyEntity
    }

    /** Send an [Embed][EmbedBuilder] to this [TextChannel]. Returns the sent [Message] or null if not sent. */
    suspend fun send(embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(embed = embed.build())))
            .value
            ?.toData(context)
            ?.lazyEntity

    /**
     * Send a [Message] with [text] and an [embed] to this [TextChannel].
     * Returns the [Message] which was sent or null if it was not sent.
     */
    suspend fun send(text: String, embed: EmbedBuilder): Message? {
        require(text.length in 1..Message.MAX_LENGTH)
        return context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(text, embed = embed.build())))
            .value
            ?.toData(context)
            ?.lazyEntity
    }

    /** Show the bot client as 'bot_name is typing...' beneath the text-entry box. */
    suspend fun sendTyping() {
        context.requester.sendRequest(Route.TriggerTypingIndicator(id))
    }
}

/** Build and Send an [Embed] to the [TextChannel]. Returns the [Message] which was sent or null if it was not sent. */
suspend inline fun TextChannel.send(embed: EmbedBuilder.() -> Unit): Message? = send(EmbedBuilder().apply(embed))

/**
 * Build and Send an [Embed] to the [TextChannel] with additional [text].
 * Returns the [Message] which was sent or null if it was not sent.
 */
suspend inline fun TextChannel.send(text: String, embed: EmbedBuilder.() -> Unit): Message? =
    send(text, EmbedBuilder().apply(embed))

/**  A representation of any [Channel] which can only be found within a [Guild]. */
interface GuildChannel : Channel {
    /** The [Guild] housing this channel. */
    val guild: Guild
    /** The sorting position of this channel in its [guild]. */
    val position: Int
    /** The displayed name of this channel in its [guild]. */
    val name: String
    /** Explicit [permission overrides][PermissionOverride] for members and roles. */
    val permissionOverrides: List<PermissionOverride>
}

/** A [TextChannel] found within a [Guild]. */
class GuildTextChannel internal constructor(
    private val data: GuildTextChannelData
) : TextChannel, GuildChannel, Mentionable {

    override val context: BotClient = data.context
    override val id: Long = data.id
    override val asMention: String get() = id.asMention(MentionType.CHANNEL)
    override val name: String get() = data.name
    override val guild: Guild get() = data.guild.lazyEntity
    override val position: Int get() = data.position.toInt()
    override val permissionOverrides: List<PermissionOverride> get() = data.permissionOverrides
    override val lastMessage: Message? get() = data.lastMessage?.lazyEntity
    override val lastPinTime: DateTimeTz? get() = data.lastPinTime
    /** The topic displayed above the message window and next to the channel name (0-1024 characters). */
    val topic: String get() = data.topic
    /**
     * Whether this channel is marked as NSFW. NSFW channels have two main differences: users have to explicitly say
     * that they are willing to view potentially unsafe-for-work content via a prompt, and these channels are exempt
     * from [explicit content filtering][Guild.explicitContentFilter].
     */
    val isNsfw: Boolean get() = data.isNsfw
    /** A configurable per-user rate limit that defines how often a user can send messages in this channel. */
    val rateLimitPerUser: Int? get() = data.rateLimitPerUser?.toInt()

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildTextChannel && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 0.toByte()
    }
}

/**
 * News channels can be interacted with the same way [GuildTextChannel] can be.
 * News channels are only available to some verified guilds "for now" - Discord Devs.
 */
class GuildNewsChannel internal constructor(
    private val data: GuildNewsChannelData
) : TextChannel, GuildChannel, Mentionable {

    override val context: BotClient = data.context
    override val id: Long = data.id
    override val asMention: String get() = id.asMention(MentionType.CHANNEL)
    override val name: String get() = data.name
    override val guild: Guild get() = data.guild.lazyEntity
    override val position: Int get() = data.position.toInt()
    override val permissionOverrides: List<PermissionOverride> get() = data.permissionOverrides
    override val lastMessage: Message? get() = data.lastMessage?.lazyEntity
    override val lastPinTime: DateTimeTz? get() = data.lastPinTime
    /** The channel topic shown next to the [name] at the top of the window. */
    val topic: String get() = data.topic
    /** `true` if the channel is marked as Not Safe For Work (NSFW). */
    val isNsfw: Boolean get() = data.isNsfw

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildNewsChannel && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 5.toByte()
    }
}


/** A special channel that has store functionality, we assume. */
class GuildStoreChannel internal constructor(private val data: GuildStoreChannelData) : GuildChannel, Mentionable {
    override val id: Long = data.id
    override val context: BotClient = data.context
    override val asMention: String get() = id.asMention(MentionType.CHANNEL)
    override val name: String get() = data.name
    override val position: Int get() = data.position.toInt()
    override val guild: Guild get() = data.guild.lazyEntity
    override val permissionOverrides: List<PermissionOverride> get() = data.permissionOverrides

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildStoreChannel && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 6.toByte()
    }
}

/** A Voice Channel (which is found within a [Guild]). */
class GuildVoiceChannel internal constructor(private val data: GuildVoiceChannelData) : GuildChannel {
    override val id: Long = data.id
    override val context: BotClient = data.context
    override val name: String get() = data.name
    override val position: Int get() = data.position.toInt()
    override val guild: Guild get() = data.guild.lazyEntity
    override val permissionOverrides: List<PermissionOverride> get() = data.permissionOverrides
    /**
     * The bitrate of the [GuildVoiceChannel] from 8 Kbps` to `96 Kbps`; basically how much data should the channel try
     * to send when people speak ([read this for more information](https://techterms.com/definition/bitrate)).
     * Going above 64 Kbps will negatively affect users on mobile or with poor connection.
     */
    val bitrate: Int get() = data.bitrate
    /**
     * The maximum number of [users][User] allowed in the [VoiceChannel][GuildVoiceChannel] at the same time.
     * The limit can be in the range `1..99`, if set to `0` there is no limit.
     */
    val userLimit: Int get() = data.userLimit.toInt()

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildVoiceChannel && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 2.toByte()
    }
}

/** A collapsible channel category (which is found within a [Guild]). */
class GuildChannelCategory internal constructor(private val data: GuildChannelCategoryData) : GuildChannel {
    override val id: Long = data.id
    override val context: BotClient = data.context
    override val name: String get() = data.name
    override val guild: Guild get() = data.guild.lazyEntity
    override val position: Int get() = data.position.toInt()
    override val permissionOverrides: List<PermissionOverride> get() = data.permissionOverrides

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is GuildChannelCategory && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 4.toByte()
    }
}

/** A Private Direct Message [TextChannel] used to talk with a single [User]. */
class DmChannel internal constructor(private val data: DmChannelData) : TextChannel {
    override val id: Long = data.id
    override val context: BotClient = data.context
    override val lastMessage: Message? get() = data.lastMessage?.lazyEntity
    override val lastPinTime: DateTimeTz? get() = data.lastPinTime
    /** The [users][User] who have access to this [DmChannel]. */
    val recipients get() = data.recipients.map { it.lazyEntity }

    /** Checks if this channel is equivalent to the [given object][other]. */
    override fun equals(other: Any?): Boolean = other is Entity && other.id == id

    companion object {
        /** A constant that defines this type of channel in Discord's API. */
        internal const val typeCode = 1.toByte()
    }
}