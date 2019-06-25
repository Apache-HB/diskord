package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.entities.toEmoji
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialEmojiPacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.serebit.strife.internal.set
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf


@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageCreatedEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val message = d.toData(context).also { channelData.messages[it.id] = it }.lazyEntity

        return MessageCreatedEvent(context, channelData.lazyEntity, message, d.id) to typeOf<MessageCreatedEvent>()
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageUpdatedEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val message = channelData.messages[d.id]?.also { it.update(d) }?.lazyEntity

        return message?.let {
            MessageUpdatedEvent(context, channelData.lazyEntity, it, d.id) to typeOf<MessageUpdatedEvent>()
        }
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageDeletedEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val message = channelData.messages[d.id]?.lazyEntity

        return MessageDeletedEvent(context, channelData.lazyEntity, message, d.id) to typeOf<MessageDeletedEvent>()
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long, val guild_id: Long? = null)
}

@Serializable
internal class MessageReactionAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageReactionAddedEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return MessageReactionAddedEvent(
            context, channel, message, d.message_id, user, d.user_id, emoji
        ) to typeOf<MessageReactionAddedEvent>()
    }

    @Serializable
    data class Data(
        val user_id: Long,
        val channel_id: Long,
        val message_id: Long,
        val guild_id: Long? = null,
        val emoji: PartialEmojiPacket
    )
}

@Serializable
internal class MessageReactionRemove(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageReactionRemovedEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return MessageReactionRemovedEvent(
            context, channel, message, d.message_id, user, d.user_id, emoji
        ) to typeOf<MessageReactionRemovedEvent>()
    }

    @Serializable
    data class Data(
        val user_id: Long,
        val channel_id: Long,
        val message_id: Long,
        val guild_id: Long? = null,
        val emoji: PartialEmojiPacket
    )
}

@Serializable
internal class MessageReactionRemoveAll(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): Pair<MessageReactionRemovedAllEvent, KType>? {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)!!
        else context.cache.getGuildTextChannelData(d.channel_id)!!

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity

        return MessageReactionRemovedAllEvent(
            context, channel, message, d.message_id
        ) to typeOf<MessageReactionRemovedAllEvent>()
    }

    @Serializable
    data class Data(val channel_id: Long, val message_id: Long, val guild_id: Long? = null)
}
