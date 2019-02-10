package com.serebit.strife.entities.channels

import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.toGuild
import com.serebit.strife.entities.toMessage
import com.serebit.strife.internal.entitydata.channels.GuildChannelCategoryData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import com.serebit.strife.internal.entitydata.channels.GuildTextChannelData
import com.serebit.strife.internal.entitydata.channels.GuildVoiceChannelData

/** A representation of any [Channel] which can only be found within a [Guild] */
interface GuildChannel : Channel {
    /** The [Guild] housing this [Channel] */
    val guild: Guild
    /** The sorting position of this [Channel] */
    val position: Int
    /**
     * The displayed name of this [Channel] in the [Guild].
     *
     * *Note: Only [GuildChannels][GuildChannel] have [names][name].*
     */
    val name: String
    /** explicit permission overwrites for members and roles */
    val permissionOverrides: List<PermissionOverride>
}

/** A [TextChannel] found within a [Guild] */
class GuildTextChannel internal constructor(private val data: GuildTextChannelData) : TextChannel, GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toGuild()
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

/** A Voice Channel (which is found within a [Guild]). */
class GuildVoiceChannel internal constructor(private val data: GuildVoiceChannelData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val position get() = data.position
    override val guild get() = data.guild.toGuild()
    override val permissionOverrides get() = data.permissionOverrides
    val bitrate get() = data.bitrate
    val userLimit get() = data.userLimit

    companion object {
        internal const val typeCode = 2
    }
}

/** A collapsible Channel Category (which is found within a [Guild]). */
class GuildChannelCategory internal constructor(private val data: GuildChannelCategoryData) : GuildChannel {
    override val id = data.id
    override val context = data.context
    override val name get() = data.name
    override val guild get() = data.guild.toGuild()
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
