package com.serebit.strife.entities

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.MessageSendPacket
import com.soywiz.klock.DateTimeTz

/** Represents a text or voice channel within Discord. */
interface Channel : Entity

interface TextChannel : Channel, Mentionable {
    val lastMessage: Message?
    val lastPinTime: DateTimeTz?
    override val asMention get() = "<#$id>"

    suspend fun send(text: String): Message? {
        require(text.length in 1..Message.MAX_LENGTH)
        return context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(text)))
            .value
            ?.toData(context)
            ?.toEntity()
    }

    suspend fun send(embed: EmbedBuilder): Message? =
        context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(embed = embed.build())))
            .value
            ?.toData(context)
            ?.toEntity()

    suspend fun send(text: String, embed: EmbedBuilder): Message? {
        require(text.length in 1..Message.MAX_LENGTH)
        return context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(text, embed = embed.build())))
            .value
            ?.toData(context)
            ?.toEntity()
    }

    suspend fun sendTyping() {
        context.requester.sendRequest(Route.TriggerTypingIndicator(id))
    }
}

suspend inline fun TextChannel.send(embed: EmbedBuilder.() -> Unit): Message? = send(EmbedBuilder().apply(embed))

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
    /** Explicit permission overrides for members and roles. */
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
    val topic get() = data.topic
    val isNsfw get() = data.isNsfw
    val rateLimitPerUser get() = data.rateLimitPerUser

    override fun equals(other: Any?) = other is GuildTextChannel && other.id == id

    companion object {
        internal const val typeCode = 0.toByte()
    }
}

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
    val bitrate get() = data.bitrate
    val userLimit get() = data.userLimit

    override fun equals(other: Any?) = other is GuildVoiceChannel && other.id == id

    companion object {
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
        internal const val typeCode = 4.toByte()
    }
}

class DmChannel internal constructor(private val data: DmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toEntity()
    override val lastPinTime get() = data.lastPinTime
    val recipients get() = data.recipients.map { it.toEntity() }

    override fun equals(other: Any?) = other is Entity && other.id == id

    companion object {
        internal const val typeCode = 1.toByte()
    }
}
