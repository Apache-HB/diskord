package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.events.*
import com.serebit.strife.getUser
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GuildChannelPacket
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

private suspend fun ChannelPacket.pullChannelData(context: BotClient) = when (this) {
    is DmChannelPacket -> context.cache.pullDmChannelData(this)
    is GuildChannelPacket -> guild_id?.let { context.cache.getGuildData(it) }
        ?.let { context.cache.pullGuildChannelData(it, this) }
    else -> throw UnsupportedOperationException("Cannot pull channel data for an unsupported channel packet type")
}

private fun ChannelPacket.removeChannelData(context: BotClient) =
    if (this !is GuildChannelPacket) context.cache.remove(RemoveCacheData.DmChannel(id))
    else context.cache.remove(RemoveCacheData.GuildChannel(id))

@Serializable
internal class ChannelCreate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelCreateEvent> =
        d.pullChannelData(context)?.lazyEntity?.let { success(ChannelCreateEvent(context, it)) }
            ?: failure("Failed to get text channel with ID ${d.id} from cache")
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelUpdateEvent> =
        d.pullChannelData(context)?.lazyEntity?.let { success(ChannelUpdateEvent(context, it)) }
            ?: failure("Failed to get text channel with ID ${d.id} from cache")
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelDeleteEvent(context, d.removeChannelData(context)?.lazyEntity, d.id))
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelPinsUpdateEvent> {
        val channelData = d.guild_id?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        channelData.update(d)

        return success(ChannelPinsUpdateEvent(context, channelData.lazyEntity))
    }

    @Serializable
    data class Data(
        val guild_id: Long?,
        val channel_id: Long,
        val last_pin_timestamp: String?
    )
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<TypingStartEvent> {
        val channelData = d.guild_id?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val user = context.getUser(d.user_id) ?: return failure("Failed to get user with ID ${d.user_id} from cache")

        return success(TypingStartEvent(context, channel, user, Instant.fromEpochMilliseconds(d.timestamp)))
    }

    @Serializable
    data class Data(val channel_id: Long, val guild_id: Long? = null, val user_id: Long, val timestamp: Long)
}
