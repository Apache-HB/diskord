package com.serebit.strife.entities.channels

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.entities.toMessage
import com.serebit.strife.internal.entitydata.channels.GuildChannelCategoryData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import com.serebit.strife.internal.entitydata.channels.GuildTextChannelData
import com.serebit.strife.internal.entitydata.channels.GuildVoiceChannelData

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

class GuildChannelCategory internal constructor(private val data: GuildChannelCategoryData) : GuildChannel {
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
    is GuildChannelCategoryData -> toChannelCategory()
    else -> throw UnknownEntityTypeException("Unknown GuildChannelData type passed to toChannel function.")
}

internal fun GuildTextChannelData.toGuildTextChannel() = GuildTextChannel(this)

internal fun GuildVoiceChannelData.toGuildVoiceChannel() = GuildVoiceChannel(this)

internal fun GuildChannelCategoryData.toChannelCategory() = GuildChannelCategory(this)
