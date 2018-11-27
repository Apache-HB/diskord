package com.serebit.diskord.internal

import com.serebit.diskord.internal.entitydata.EntityData
import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.entitydata.MessageData
import com.serebit.diskord.internal.entitydata.RoleData
import com.serebit.diskord.internal.entitydata.UserData
import com.serebit.diskord.internal.entitydata.channels.ChannelData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.channels.update
import com.serebit.diskord.internal.entitydata.findById
import com.serebit.diskord.internal.entitydata.removeById
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.EntityPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.packets.RolePacket
import com.serebit.diskord.internal.packets.UserPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class EntityDataCache : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val guilds = mutableMapOf<Long, GuildData>()
    private val dmChannels = mutableMapOf<Long, DmChannelData>()
    private val groupDmChannels = mutableMapOf<Long, GroupDmChannelData>()
    private val users = mutableMapOf<Long, UserData>()

    fun <T : EntityData> cache(data: T) {
        when (data) {
            is GuildData -> guilds[data.id] = data
            is GuildChannelData -> guilds[data.guildId]?.allChannels?.put(data.id, data)
            is DmChannelData -> dmChannels[data.id] = data
            is GroupDmChannelData -> groupDmChannels[data.id] = data
            is MessageData -> data.channel.messages.add(data)
            is UserData -> users[data.id] = data
        }
    }

    fun <T : EntityPacket> update(packet: T) {
        when (packet) {
            is GuildUpdatePacket -> guilds[packet.id]?.update(packet)
            is GuildChannelPacket -> guilds[packet.guild_id]?.allChannels?.get(packet.id)?.update(packet)
            is RolePacket -> findRole(packet.id)?.update(packet)
            is DmChannelPacket -> dmChannels[packet.id]?.update(packet)
            is GroupDmChannelPacket -> groupDmChannels[packet.id]?.update(packet)
            is PartialMessagePacket -> findMessage(packet.id, packet.channel_id)?.update(packet)
            is UserPacket -> users[packet.id]?.update(packet)
        }
    }

    fun findGuild(id: Long) = guilds[id]

    fun findUser(id: Long) = users[id]

    fun findDmChannel(id: Long) = dmChannels[id]

    fun findGroupDmChannel(id: Long) = groupDmChannels[id]

    suspend fun findGuildChannel(id: Long) = guilds.values.map {
        async { it.allChannels[id] }
    }.awaitAll().filterNotNull().firstOrNull()

    inline fun <reified T : ChannelData> findChannel(id: Long): T? = runBlocking {
        mutableListOf(
            async { findDmChannel(id) as? T },
            async { findGroupDmChannel(id) as? T },
            async { findGuildChannel(id) as? T }
        ).awaitAll().filterNotNull().firstOrNull()
    }

    fun findRole(id: Long): RoleData? = runBlocking {
        guilds.values.map {
            async { it.roles.findById(id) }
        }.awaitAll().filterNotNull().firstOrNull()
    }

    fun removeGuild(id: Long) = guilds.minusAssign(id)

    fun removeChannel(id: Long) {
        when (val channel = findChannel<ChannelData>(id)) {
            null -> Unit
            is GuildChannelData -> guilds[channel.guildId]?.allChannels?.get(id)
            is DmChannelData -> dmChannels -= channel.id
            is GroupDmChannelData -> groupDmChannels -= channel.id
        }
    }
}

internal fun <T : EntityData, C : Collection<T>> EntityDataCache.cacheAll(collection: C) =
    collection.forEach { cache(it) }

internal fun EntityDataCache.findMessage(id: Long, channelId: Long): MessageData? =
    findChannel<TextChannelData>(channelId)?.messages?.findById(id)

internal fun EntityDataCache.removeMessage(id: Long, channelId: Long) {
    findChannel<TextChannelData>(channelId)?.messages?.removeById(id)
}
