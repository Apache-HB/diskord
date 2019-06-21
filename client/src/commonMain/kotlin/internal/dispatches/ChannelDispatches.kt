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
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelCreateEvent(
        context, context.cache.pushChannelData(d.toTypedPacket()).lazyEntity
    ) to typeOf<ChannelCreateEvent>()
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelUpdateEvent(
        context, context.cache.pullChannelData(d.toTypedPacket()).lazyEntity
    ) to typeOf<ChannelUpdateEvent>()
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelDeleteEvent(
        context, context.cache.pullChannelData(d.toTypedPacket()).lazyEntity, d.id
    ) to typeOf<ChannelDeleteEvent>()
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<ChannelPinsUpdateEvent, KType>? {
        val channelData = context.cache.getTextChannelData(d.channel_id) ?: return null
        d.last_pin_timestamp?.let { channelData.lastPinTime = DateFormat.ISO_WITH_MS.parse(it) }

        return ChannelPinsUpdateEvent(context, channelData.lazyEntity) to typeOf<ChannelPinsUpdateEvent>()
    }

    @Serializable
    data class Data(val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<Event, KType>? {
        val channel = context.cache.getTextChannelData(d.channel_id)?.lazyEntity ?: return null
        val user = context.cache.getUserData(d.user_id)?.lazyEntity ?: return null

        return TypingStartEvent(context, channel, user, DateTime(d.timestamp)) to typeOf<TypingStartEvent>()
    }

    @Serializable
    data class Data(val channel_id: Long, val user_id: Long, val timestamp: Long)
}
