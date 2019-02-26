package com.serebit.strife.entities

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.internal.entitydata.*
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
            ?.toEntity()

    // TODO fun can(Permission) or hasPerm(Permission)
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
    override val guild get() = data.guild.toEntity()
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
    override val guild get() = data.guild.toEntity()
    override val position get() = data.position.toInt()
    override val permissionOverrides get() = data.permissionOverrides

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

    companion object {
        internal const val typeCode = 1.toByte()
    }
}
