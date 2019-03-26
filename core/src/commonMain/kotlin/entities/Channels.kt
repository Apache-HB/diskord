package com.serebit.strife.entities

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.internal.entitydata.DmChannelData
import com.serebit.strife.internal.entitydata.GuildChannelCategoryData
import com.serebit.strife.internal.entitydata.GuildNewsChannelData
import com.serebit.strife.internal.entitydata.GuildStoreChannelData
import com.serebit.strife.internal.entitydata.GuildTextChannelData
import com.serebit.strife.internal.entitydata.GuildVoiceChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.MessageRoute
import com.soywiz.klock.DateTimeTz

/** Represents a text or voice channel within Discord. */
interface Channel : Entity

/** A [Channel] used to send textual messages with optional attachments. */
interface TextChannel : Channel {
    /** The [id][Message.id] of the last [Message] sent in this [TextChannel]. */
    val lastMessage: Message?
    /** The [time][DateTimeTz] of the last time a [Message] was pinned in this [TextChannel]. */
    val lastPinTime: DateTimeTz?

    /** Send a [message] to this [TextChannel]. Returns the [Message] which was sent or null if it was not sent. */
    suspend fun send(message: String) =
        context.requester.sendRequest(MessageRoute.Create(id), data = mapOf("content" to message)).value
            ?.toData(context)
            ?.toEntity()
}

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

/** A [TextChannel] found within a [Guild] */
class GuildTextChannel internal constructor(private val data: GuildTextChannelData) : TextChannel, GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toEntity()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides
    override val lastMessage get() = data.lastMessage?.toEntity()
    override val lastPinTime get() = data.lastPinTime
    /** The [TextChannel] topic displayed above the [Message] window (0-1024 characters). */
    val topic get() = data.topic
    /**
     * Whether this [TextChannel] is marked as `NSFW`.
     * [Users][User] must confirm they are comfortable seeing the content in an `NSFW` [channel][TextChannel].
     * `NSFW` [channels][TextChannel] are exempt from [explicit content filtering][Guild.explicitContentFilter].
     */
    val isNsfw get() = data.isNsfw
    /** Docs TODO */
    val rateLimitPerUser get() = data.rateLimitPerUser

    override fun equals(other: Any?) = other is GuildTextChannel && other.id == id

    companion object {
        /**
         * The type [integer][Int] of this type of [Channel].
         * [see](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types).
         */
        internal const val typeCode = 0.toByte()
    }
}

/**
 * News channels can be interacted with the same way [GuildTextChannel] can be.
 * News channels are only available to some verified guilds "for now" - Discord Devs.
 */
class GuildNewsChannel internal constructor(private val data: GuildNewsChannelData) : TextChannel, GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toEntity()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides
    override val lastMessage get() = data.lastMessage?.toEntity()
    override val lastPinTime get() = data.lastPinTime
    val topic get() = data.topic
    val isNsfw get() = data.isNsfw

    companion object {
        internal const val typeCode = 5.toByte()
    }
}

/**
 * TODO Docs
 */
class GuildStoreChannel internal constructor(private val data: GuildStoreChannelData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position.toInt()
    override val guild get() = data.guild.toEntity()
    override val permissionOverrides get() = data.permissionOverrides

    override fun equals(other: Any?) = other is GuildStoreChannel && other.id == id

    companion object {
        internal const val typeCode = 6.toByte()
    }
}

/** A Voice Channel (which is found within a [Guild]). */
class GuildVoiceChannel internal constructor(private val data: GuildVoiceChannelData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position.toInt()
    override val guild get() = data.guild.toEntity()
    override val permissionOverrides get() = data.permissionOverrides
    /**
     * The bitrate of the [GuildVoiceChannel] from `8kb/s` to `96kb/s`; basically how much data should the channel try
     * to send when people speak ([read this for more information](https://techterms.com/definition/bitrate)).
     * Going above `64kp/s` will negatively affect [users][User] on mobile or with poor connection.
     */
    val bitrate get() = data.bitrate
    /**
     * The maximum number of [users][User] allowed in the [VoiceChannel][GuildVoiceChannel] at the same time.
     * The limit can be from `1`-`99`, if set to `0` there is no limit.
     */
    val userLimit get() = data.userLimit

    override fun equals(other: Any?) = other is GuildVoiceChannel && other.id == id

    companion object {
        /**
         * The type [integer][Int] of this type of [Channel].
         * [see](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types).
         */
        internal const val typeCode = 2.toByte()
    }
}

/** A collapsible Channel Category (which is found within a [Guild]). */
class GuildChannelCategory internal constructor(private val data: GuildChannelCategoryData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toEntity()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides

    override fun equals(other: Any?) = other is GuildChannelCategory && other.id == id

    companion object {
        /**
         * The type [integer][Int] of this type of [Channel].
         * [see](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types).
         */
        internal const val typeCode = 4.toByte()
    }
}

/** A Private Direct Message [TextChannel] used to talk with a single [User]. */
class DmChannel internal constructor(private val data: DmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toEntity()
    override val lastPinTime get() = data.lastPinTime
    /** The [users][User] who have access to this [DmChannel]. */
    val recipients get() = data.recipients.map { it.toEntity() }

    override fun equals(other: Any?) = other is Entity && other.id == id

    companion object {
        /**
         * The type [integer][Int] of this type of [Channel].
         * [see](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types).
         */
        internal const val typeCode = 1.toByte()
    }
}
