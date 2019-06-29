package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.GuildChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable

private suspend fun GenericChannelPacket.pullChannelData(context: BotClient) = toTypedPacket()
    .let {
        if (it is DmChannelPacket)
            context.cache.pullDmChannelData(it)
        else
            context.cache.pullGuildChannelData(context.cache.getGuildData(guild_id!!)!!, it as GuildChannelPacket)
    }

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) = success(
        ChannelCreateEvent(context, d.pullChannelData(context).lazyEntity))
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelUpdateEvent(context, d.pullChannelData(context).lazyEntity))
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelDeleteEvent(context, d.pullChannelData(context).lazyEntity, d.id))
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelPinsUpdateEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        channelData.update(d)

        return success(ChannelPinsUpdateEvent(context, channelData.lazyEntity))
    }

    @Serializable
    data class Data(val guild_id: Long? = null, val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<TypingStartEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val user = context.getUser(d.user_id)
            ?: return failure("Failed to get user with ID ${d.user_id} from cache")

        return success(TypingStartEvent(context, channel, user, DateTime(d.timestamp)))
    }

    @Serializable
    data class Data(val channel_id: Long, val guild_id: Long? = null, val user_id: Long, val timestamp: Long)
}
