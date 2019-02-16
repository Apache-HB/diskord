package com.serebit.strife.entities

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.entitydata.channels.*
import com.serebit.strife.internal.network.Endpoint
import com.soywiz.klock.DateTimeTz

/** Represents a text or voice channel within Discord. */
interface Channel : Entity

interface TextChannel : Channel {
    val lastMessage: Message?
    val lastPinTime: DateTimeTz?

    suspend fun send(message: String) =
        context.requester.sendRequest(Endpoint.CreateMessage(id), data = mapOf("content" to message))
            .value
            ?.toData(context)
            ?.toMessage()
}

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
class GuildTextChannel internal constructor(private val data: GuildTextChannelData) : TextChannel,
    GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toGuild()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val topic get() = data.topic
    val isNsfw get() = data.isNsfw

    companion object {
        internal const val typeCode = 0.toByte()
    }
}

/** A Voice Channel (which is found within a [Guild]). */
class GuildVoiceChannel internal constructor(private val data: GuildVoiceChannelData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position.toInt()
    override val guild get() = data.guild.toGuild()
    override val permissionOverrides get() = data.permissionOverrides
    val bitrate get() = data.bitrate
    val userLimit get() = data.userLimit

    companion object {
        internal const val typeCode = 2.toByte()
    }
}

/** A collapsible Channel Category (which is found within a [Guild]). */
class GuildChannelCategory internal constructor(private val data: GuildChannelCategoryData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toGuild()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides

    companion object {
        internal const val typeCode = 4.toByte()
    }
}

internal fun GuildChannelData.toGuildChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is GuildVoiceChannelData -> toGuildVoiceChannel()
    is GuildChannelCategoryData -> toChannelCategory()
    else -> throw UnknownEntityTypeException("Unknown GuildChannelData type passed to toChannel function.")
}

class DmChannel internal constructor(private val data: DmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val recipients get() = data.recipients.map { it.toUser() }

    companion object {
        internal const val typeCode = 1.toByte()
    }
}

class GroupDmChannel internal constructor(private val data: GroupDmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val name get() = data.name
    val recipients get() = data.recipients.map { it.toUser() }
    val owner get() = data.owner.toUser()

    companion object {
        internal const val typeCode = 3.toByte()
    }
}

internal fun ChannelData.toChannel() = when (this) {
    is GuildChannelData -> toGuildChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}

internal fun TextChannelData.toTextChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is DmChannelData -> toDmChannel()
    is GroupDmChannelData -> toGroupDmChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}

internal fun GuildTextChannelData.toGuildTextChannel() = GuildTextChannel(this)

internal fun GuildVoiceChannelData.toGuildVoiceChannel() = GuildVoiceChannel(this)

internal fun GuildChannelCategoryData.toChannelCategory() = GuildChannelCategory(this)

internal fun DmChannelData.toDmChannel() = DmChannel(this)

internal fun GroupDmChannelData.toGroupDmChannel() = GroupDmChannel(this)
