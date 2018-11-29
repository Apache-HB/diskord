package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.PermissionOverride
import com.serebit.diskord.data.UnknownEntityTypeException
import com.serebit.diskord.entities.toMessage
import com.serebit.diskord.internal.entitydata.channels.ChannelCategoryData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildVoiceChannelData

interface GuildChannel : Channel {
    val position: Int
    val name: String
    val permissionOverrides: List<PermissionOverride>
}

class GuildTextChannel internal constructor(private val data: GuildTextChannelData) : TextChannel, GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position
    override val permissionOverrides get() = data.permissionOverrides
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val topic get() = data.topic
    val isNsfw get() = data.isNsfw

    companion object {
        internal const val typeCode = 0
    }
}

class GuildVoiceChannel internal constructor(private val data: GuildVoiceChannelData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position
    override val permissionOverrides get() = data.permissionOverrides
    val bitrate get() = data.bitrate
    val userLimit get() = data.userLimit

    companion object {
        internal const val typeCode = 2
    }
}

class ChannelCategory internal constructor(private val data: ChannelCategoryData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position
    override val permissionOverrides get() = data.permissionOverrides

    companion object {
        internal const val typeCode = 4
    }
}

internal fun GuildChannelData.toGuildChannel() = when (this) {
    is GuildTextChannelData -> toGuildTextChannel()
    is GuildVoiceChannelData -> toGuildVoiceChannel()
    is ChannelCategoryData -> toChannelCategory()
    else -> throw UnknownEntityTypeException("Unknown GuildChannelData type passed to toChannel function.")
}

internal fun GuildTextChannelData.toGuildTextChannel() = GuildTextChannel(this)

internal fun GuildVoiceChannelData.toGuildVoiceChannel() = GuildVoiceChannel(this)

internal fun ChannelCategoryData.toChannelCategory() = ChannelCategory(this)
