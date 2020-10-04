package com.serebit.strife.internal.dispatches

import com.serebit.strife.BotClient
import com.serebit.strife.GetCacheData
import com.serebit.strife.RemoveCacheData
import com.serebit.strife.entities.toEmoji
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialEmojiPacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import kotlinx.serialization.Serializable

private suspend fun BotClient.fetchChannelData(channelID: Long, guildID: Long?) =
    guildID?.let { cache.getGuildData(it) }?.let { obtainGuildTextChannelData(channelID) }
        ?: obtainDmChannelData(channelID)

@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageCreateEvent> {
        val channelData = context.fetchChannelData(d.channel_id, d.guild_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = context.cache.pushMessageData(d).lazyEntity

        return success(MessageCreateEvent(context, channelData.lazyEntity, message, d.id))
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageEditEvent> {
        val channelData = context.fetchChannelData(d.channel_id, d.guild_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache")

        val message = context.obtainMessageData(d.id, d.channel_id)?.also { it.update(d) }
            ?: return failure("Failed to get message with ID ${d.id} in channel ${d.channel_id} from API")

        return success(MessageEditEvent(context, channelData.lazyEntity, message.lazyEntity, d.id))
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageDeleteEvent> {
        val channelData = context.fetchChannelData(d.channel_id, d.guild_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache or API")

        val message = context.cache.get(GetCacheData.Message(d.id, d.channel_id))?.lazyEntity?.also {
            context.cache.remove(RemoveCacheData.Message(d.id, d.channel_id))
        }

        return success(MessageDeleteEvent(context, channelData.lazyEntity, message, d.id))
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long, val guild_id: Long? = null)
}

@Serializable
internal class MessageDeleteBulk(override val s: Int, override val d: Data) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageBulkDeleteEvent> {
        val channelData = context.fetchChannelData(d.channel_id, d.guild_id)
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache or API")

        val messages = d.ids.associateWith { id ->
            context.cache.get(GetCacheData.Message(id, d.channel_id))?.lazyEntity?.also {
                context.cache.remove(RemoveCacheData.Message(id, d.channel_id))
            }
        }

        return success(MessageBulkDeleteEvent(context, channelData.lazyEntity, messages))
    }

    @Serializable
    data class Data(val ids: List<Long>, val channel_id: Long, val guild_id: Long? = null)
}

@Serializable
internal class MessageReactionAdd(override val s: Int, override val d: Data) : DispatchPayload() {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionAddEvent> {
        val channel = context.fetchChannelData(d.channel_id, d.guild_id)?.lazyEntity
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache or API")

        val message = context.obtainMessageData(d.message_id, d.channel_id)?.lazyEntity
            ?: return failure("Failed to get message with ID ${d.message_id} in channel ${d.channel_id} from API")

        val user = context.cache.get(GetCacheData.User(d.user_id))?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return success(MessageReactionAddEvent(context, channel, message, d.message_id, user, d.user_id, emoji))
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
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionRemoveEvent> {
        val channel = context.fetchChannelData(d.channel_id, d.guild_id)?.lazyEntity
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache or API")

        val message = context.obtainMessageData(d.message_id, d.channel_id)?.lazyEntity
            ?: return failure("Failed to get message with ID ${d.message_id} in channel ${d.channel_id} from API")

        val user = context.cache.get(GetCacheData.User(d.user_id))?.lazyEntity
        val emoji = d.emoji.toEmoji(context)

        return success(MessageReactionRemoveEvent(context, channel, message, d.message_id, user, d.user_id, emoji))
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
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun asEvent(context: BotClient): DispatchConversionResult<MessageReactionRemoveAllEvent> {
        val channel = context.fetchChannelData(d.channel_id, d.guild_id)?.lazyEntity
            ?: return failure("Failed to get text channel with ID ${d.channel_id} from cache or API")

        val message = context.obtainMessageData(d.message_id, d.channel_id)?.lazyEntity

        return success(MessageReactionRemoveAllEvent(context, channel, message, d.message_id))
    }

    @Serializable
    data class Data(val channel_id: Long, val message_id: Long, val guild_id: Long? = null)
}
