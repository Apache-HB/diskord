package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.GuildChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private fun GenericChannelPacket.pullChannelData(context: BotClient) = toTypedPacket()
    .let {
        if (it is DmChannelPacket)
            context.cache.pullDmChannelData(it)
        else
            context.cache.pullGuildChannelData(context.cache.getGuildData(guild_id!!)!!, it as GuildChannelPacket)
    }

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelCreateEvent(
        context, d.pullChannelData(context).lazyEntity
    ) to typeOf<ChannelCreateEvent>()
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelUpdateEvent(
        context, d.pullChannelData(context).lazyEntity
    ) to typeOf<ChannelUpdateEvent>()
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient) = ChannelDeleteEvent(
        context, d.pullChannelData(context).lazyEntity, d.id
    ) to typeOf<ChannelDeleteEvent>()
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<ChannelPinsUpdateEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        d.last_pin_timestamp?.let { channelData.lastPinTime = DateFormat.ISO_WITH_MS.parse(it) }

        return ChannelPinsUpdateEvent(context, channelData.lazyEntity) to typeOf<ChannelPinsUpdateEvent>()
    }

    @Serializable
    data class Data(val guild_id: Long? = null, val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<Event, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val channel = channelData.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity ?: return null

        return TypingStartEvent(context, channel, user, DateTime(d.timestamp)) to typeOf<TypingStartEvent>()
    }

    @Serializable
    data class Data(val channel_id: Long, val guild_id: Long? = null, val user_id: Long, val timestamp: Long)
}
