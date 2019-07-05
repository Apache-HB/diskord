package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.*
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable

private suspend fun GenericChannelPacket.pullChannelData(context: BotClient) = toTypedPacket().let { packet ->
    if (packet is DmChannelPacket)
        context.cache.pullDmChannelData(packet)
    else
        getGuildData(context)?.let { context.cache.pullGuildChannelData(it, packet as GuildChannelPacket) }
}

private fun GenericChannelPacket.removeChannelData(context: BotClient) =
    if (guild_id == null) context.cache.removeDmChannelData(id) else context.cache.removeGuildChannelData(id)

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelCreateEvent> =
        d.pullChannelData(context)?.lazyEntity?.let { success(ChannelCreateEvent(context, it)) }
            ?: failure("Failed to get text channel with ID ${d.id} in guild ${d.guild_id} from cache")
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelUpdateEvent> =
        d.pullChannelData(context)?.lazyEntity?.let { success(ChannelUpdateEvent(context, it)) }
            ?: failure("Failed to get text channel with ID ${d.id} in guild ${d.guild_id} from cache")
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelDeleteEvent(context, d.removeChannelData(context)?.lazyEntity, d.id))
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelPinsUpdateEvent> {
        val channelData = d.getGuildData(context)?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        channelData.update(d)

        return success(ChannelPinsUpdateEvent(context, channelData.lazyEntity))
    }

    @Serializable
    data class Data(
        override val guild_id: Long? = null,
        val channel_id: Long,
        val last_pin_timestamp: String?
    ) : GuildablePacket
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<TypingStartEvent> {
        val channelData = d.getGuildData(context)?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val user = context.getUser(d.user_id) ?: return failure("Failed to get user with ID ${d.user_id} from cache")

        return success(TypingStartEvent(context, channel, user, DateTime(d.timestamp)))
    }

    @Serializable
    data class Data(
        val channel_id: Long,
        override val guild_id: Long? = null,
        val user_id: Long,
        val timestamp: Long
    ) : GuildablePacket
}
