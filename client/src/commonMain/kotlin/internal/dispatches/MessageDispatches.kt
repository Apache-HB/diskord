package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.GetCacheData
import com.serebit.strife.entities.toEmoji
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialEmojiPacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import kotlinx.serialization.Serializable


@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageCreatedEvent> {
        val channelData =
            d.guild_id?.let { context.cache.getGuildData(it) }?.let { context.obtainGuildTextChannelData(d.channel_id) }
                ?: context.obtainDmChannelData(d.channel_id)
                ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = channelData.update(d).lazyEntity

        return success(MessageCreatedEvent(context, channelData.lazyEntity, message, d.id))
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageUpdatedEvent> {
        val channelData =
            d.guild_id?.let { context.cache.getGuildData(it) }?.let { context.obtainGuildTextChannelData(d.channel_id) }
                ?: context.obtainDmChannelData(d.channel_id)
                ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = channelData.getMessageData(d.id)?.also { it.update(d) }
            ?: context.requester.sendRequest(Route.GetChannelMessage(d.channel_id, d.id))
                .value
                ?.let { channelData.update(it) }
            ?: return failure("Failed to get message with ID ${d.id} in channel ${d.channel_id} from API")

        return success(MessageUpdatedEvent(context, channelData.lazyEntity, message.lazyEntity, d.id))
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageDeletedEvent> {
        val channelData =
            d.guild_id?.let { context.cache.getGuildData(it) }?.let { context.obtainGuildTextChannelData(d.channel_id) }
                ?: context.obtainDmChannelData(d.channel_id)
                ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = channelData.getMessageData(d.id)?.lazyEntity

        return success(MessageDeletedEvent(context, channelData.lazyEntity, message, d.id))
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long, val guild_id: Long? = null)
}

@Serializable
internal class MessageReactionAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionAddedEvent> {
        val channelData = d.guild_id?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.getMessageData(d.message_id)?.lazyEntity
        val user = context.cache.get(GetCacheData.User(d.user_id))?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return success(MessageReactionAddedEvent(context, channel, message, d.message_id, user, d.user_id, emoji))
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
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionRemovedEvent> {
        val channelData = d.guild_id?.let { context.obtainGuildTextChannelData(d.channel_id) }
            ?: context.obtainDmChannelData(d.channel_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.getMessageData(d.message_id)?.lazyEntity
        val user = context.cache.get(GetCacheData.User(d.user_id))?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return success(MessageReactionRemovedEvent(context, channel, message, d.message_id, user, d.user_id, emoji))
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
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionRemovedAllEvent> {
        val channelData =
            d.guild_id?.let { context.cache.getGuildData(it) }?.let { context.obtainGuildTextChannelData(d.channel_id) }
                ?: context.obtainDmChannelData(d.channel_id)
                ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.getMessageData(d.message_id)?.lazyEntity

        return success(MessageReactionRemovedAllEvent(context, channel, message, d.message_id))
    }

    @Serializable
    data class Data(val channel_id: Long, val message_id: Long, val guild_id: Long? = null)
}
