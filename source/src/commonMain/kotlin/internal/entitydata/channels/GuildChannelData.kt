package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.PermissionOverride
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

internal interface GuildChannelData : ChannelData {
    val guildId: Long?
    var position: Int
    var name: String
    var isNsfw: Boolean
    var permissionOverrides: List<PermissionOverride>
    var parent: ChannelCategoryData?
}

internal fun GuildChannelData.update(packet: GuildChannelPacket) = when (this) {
    is GuildTextChannelData -> update(packet as GuildTextChannelPacket)
    is GuildVoiceChannelData -> update(packet as GuildVoiceChannelPacket)
    is ChannelCategoryData -> update(packet as ChannelCategoryPacket)
    else -> throw IllegalStateException("Attempted to update an unknown GuildChannelData type.")
}

internal fun GuildChannelPacket.toData(context: Context) = when (this) {
    is GuildTextChannelPacket -> GuildTextChannelData(this, context)
    is GuildVoiceChannelPacket -> GuildVoiceChannelData(this, context)
    is ChannelCategoryPacket -> ChannelCategoryData(this, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}
