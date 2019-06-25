package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) = success(
        ChannelCreateEvent(context, context.cache.pushChannelData(d.toTypedPacket()).lazyEntity))
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelUpdateEvent(context, context.cache.pullChannelData(d.toTypedPacket()).lazyEntity))
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient) =
        success(ChannelDeleteEvent(context, context.cache.pullChannelData(d.toTypedPacket()).lazyEntity, d.id))
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<ChannelPinsUpdateEvent> {
        val channelData = context.cache.getTextChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        d.last_pin_timestamp?.let { channelData.lastPinTime = DateFormat.ISO_WITH_MS.parse(it) }

        return success(ChannelPinsUpdateEvent(context, channelData.lazyEntity))
    }

    @Serializable
    data class Data(val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<Event> {
        val channel = context.cache.getTextChannelData(d.channel_id)?.lazyEntity
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val user = context.cache.getUserData(d.user_id)?.lazyEntity
            ?: return failure("Failed to get user with ID ${d.user_id} from cache")

        return success(TypingStartEvent(context, channel, user, DateTime(d.timestamp)))
    }

    @Serializable
    data class Data(val channel_id: Long, val user_id: Long, val timestamp: Long)
}
