package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.entities.toEmoji
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.*
import kotlinx.serialization.Serializable

private suspend fun obtainChannelData(id: Long, context: BotClient) = context.cache.getTextChannelData(id)
    ?: context.requester.sendRequest(Route.GetChannel(id))
        .value
        ?.let { it.toTypedPacket() as GuildTextChannelPacket }
        ?.toData(context)

@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): MessageCreatedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val message = d.toData(context).also { channelData.messages[it.id] = it }.lazyEntity

        return MessageCreatedEvent(context, channelData.lazyEntity, message)
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): MessageUpdatedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val message = channelData.messages[d.id]?.also { it.update(d) }?.lazyEntity

        return message?.let { MessageUpdatedEvent(context, channelData.lazyEntity, it) }
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): MessageDeletedEvent? {
        val channelData = obtainChannelData(d.channel_id, context)
            ?.also { it.messages.remove(d.id) }
            ?: return null
        val message = channelData.messages[d.id]?.lazyEntity

        return MessageDeletedEvent(context, channelData.lazyEntity, message, d.id)
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long)
}

@Serializable
internal class MessageReactionAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: BotClient): MessageReactionAddedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return MessageReactionAddedEvent(context, channel, message, d.message_id, user, d.user_id, emoji)
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
    override suspend fun asEvent(context: BotClient): MessageReactionRemovedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return MessageReactionRemovedEvent(context, channel, message, d.message_id, user, d.user_id, emoji)
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
    override suspend fun asEvent(context: BotClient): MessageReactionRemovedAllEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity

        return MessageReactionRemovedAllEvent(context, channel, message, d.message_id)
    }

    @Serializable
    data class Data(val channel_id: Long, val message_id: Long, val guild_id: Long? = null)
}