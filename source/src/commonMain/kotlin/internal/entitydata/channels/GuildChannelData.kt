package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.PermissionOverride
import com.serebit.diskord.internal.packets.GuildChannelCategoryPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

internal interface GuildChannelData : ChannelData {
    var guildId: Long?
    var position: Int
    var name: String
    var isNsfw: Boolean
    var permissionOverrides: List<PermissionOverride>
    var parentId: Long?
}

internal fun GuildChannelData.update(packet: GuildChannelPacket) = when (this) {
    is GuildTextChannelData -> update(packet as GuildTextChannelPacket)
    is GuildVoiceChannelData -> update(packet as GuildVoiceChannelPacket)
    is GuildChannelCategoryData -> update(packet as GuildChannelCategoryPacket)
    else -> throw IllegalStateException("Attempted to update an unknown GuildChannelData type.")
}

internal fun GuildChannelPacket.toGuildChannelData(context: Context) = when (this) {
    is GuildTextChannelPacket -> GuildTextChannelData(this, context)
    is GuildVoiceChannelPacket -> GuildVoiceChannelData(this, context)
    is GuildChannelCategoryPacket -> GuildChannelCategoryData(this, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}
