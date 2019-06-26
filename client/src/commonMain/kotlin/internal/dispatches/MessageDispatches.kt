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


@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageCreatedEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = d.toData(context).also { channelData.messages[it.id] = it }.lazyEntity

        return success(MessageCreatedEvent(context, channelData.lazyEntity, message, d.id))
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageUpdatedEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = channelData.messages[d.id]?.also { it.update(d) }?.lazyEntity
            ?: return failure("Failed to get message with ID ${d.id} in channel ${d.channel_id}")

        return success(MessageUpdatedEvent(context, channelData.lazyEntity, message, d.id))
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageDeletedEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = channelData.messages[d.id]?.lazyEntity

        return success(MessageDeletedEvent(context, channelData.lazyEntity, message, d.id))
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long, val guild_id: Long? = null)
}

@Serializable
internal class MessageReactionAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionAddedEvent> {
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
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
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity
        val user = context.cache.getUserData(d.user_id)?.lazyEntity
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
        val channelData = if (d.guild_id == null) context.obtainDmChannelData(d.channel_id)
        else context.cache.getGuildTextChannelData(d.channel_id)

        channelData ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val channel = channelData.lazyEntity
        val message = channelData.messages[d.message_id]?.lazyEntity

        return success(MessageReactionRemovedAllEvent(context, channel, message, d.message_id))
    }

    @Serializable
    data class Data(val channel_id: Long, val message_id: Long, val guild_id: Long? = null)
}
