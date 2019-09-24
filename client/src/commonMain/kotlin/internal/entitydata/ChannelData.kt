package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.toOverrides
import com.serebit.strife.entities.*
import com.serebit.strife.internal.ISO
import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.set
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal interface ChannelData<U : ChannelPacket, E : Channel> : EntityData<U, E>

internal interface TextChannelData<U : TextChannelPacket, E : TextChannel> : ChannelData<U, E> {
    val messageList: List<MessageData>
    val lastMessage: MessageData?
    val lastPinTime: DateTimeTz?

    fun update(data: ChannelPinsUpdate.Data)

    fun update(data: MessageCreatePacket): MessageData

    fun getMessageData(id: Long): MessageData?

    /**
     * Send a [Message] with [text] and an [embed] to this [TextChannel].
     * Returns the [MessageData] which was sent or null if it was not sent.
     * Requires the [TextChannelData] root of this [TextChannel].
     */
    suspend fun send(
        text: String? = null,
        embed: EmbedBuilder? = null,
        tts: Boolean = false
    ): MessageData? {
        text?.run {
            require(length in 1..Message.MAX_LENGTH) {
                "Message.text length must be within allowed range (1..${Message.MAX_LENGTH}"
            }
        }
        return context.requester.sendRequest(Route.CreateMessage(id, MessageSendPacket(text, tts, embed?.build())))
            .value
            ?.toData(this, context)
    }

    /**
     * Returns a flow of this channel's [Message]s with an optional [limit] and either [before] or [after]
     * @param before The message id to get messages before.
     * @param after The message id to get messages after.
     * @param limit The max number of messages to return. Whole history is returned if not specified.
     * */
    suspend fun flowOfMessages(before: Long? = null, after: Long? = null, limit: Int? = null): Flow<Message> {
        val data = this
        return flow {
            var count = 0

            var lastMessage = when {
                before != null -> before
                after != null -> after
                else -> null
            }

            do {
                val apiLimit = if (limit != null && limit - count < 100) limit - count else 100

                val messageList = when {
                    before != null -> context.requester.sendRequest(
                        Route.GetChannelMessages(
                            id,
                            before = lastMessage,
                            limit = apiLimit
                        )
                    ).value
                    after != null -> context.requester.sendRequest(
                        Route.GetChannelMessages(
                            id,
                            after = lastMessage,
                            limit = apiLimit
                        )
                    ).value?.asReversed()
                    else -> context.requester.sendRequest(Route.GetChannelMessages(id)).value
                }

                val size = messageList?.size ?: 0

                messageList?.forEachIndexed { index, messageCreatePacket ->
                    val message = messageCreatePacket.toData(data, context).lazyEntity
                    if (index == size - 1) lastMessage = message.id
                    emit(message)
                }

                count += size
            } while (size > 0 && limit?.let { count < it } != false)
        }
    }

}

internal interface GuildChannelData<U : GuildChannelPacket, E : GuildChannel> : ChannelData<U, E> {
    val guild: GuildData
    val position: Short
    val name: String
    val permissionOverrides: List<PermissionOverride>
    val parentID: Long?
}

internal interface GuildMessageChannelData<
        U : GuildMessageChannelPacket, E : GuildMessageChannel
        > : GuildChannelData<U, E>, TextChannelData<U, E> {
    val topic: String
    val isNsfw: Boolean
}

internal class GuildTextChannelData(
    packet: GuildTextChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildMessageChannelData<GuildTextChannelPacket, GuildTextChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildTextChannel(this) }
    private val messages = LruWeakCache<Long, MessageData>()
    override val messageList get() = messages.values
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    override var position = packet.position
        private set
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
        private set
    override var name = packet.name
        private set
    override var isNsfw = packet.nsfw
        private set
    override var parentID = packet.parent_id
        private set
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO.parse(it) }
        private set
    override var topic = packet.topic.orEmpty()
        private set
    var rateLimitPerUser = packet.rate_limit_per_user
        private set

    override fun update(packet: GuildTextChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        topic = packet.topic.orEmpty()
        isNsfw = packet.nsfw
        parentID = packet.parent_id
        rateLimitPerUser = packet.rate_limit_per_user
    }

    override fun update(data: ChannelPinsUpdate.Data) {
        data.last_pin_timestamp?.let { lastPinTime = DateFormat.ISO.parse(it) }
    }

    override fun update(data: MessageCreatePacket) = data.toData(this, context).also { messages[it.id] = it }

    override fun getMessageData(id: Long) = messages[id]
}

internal class GuildNewsChannelData(
    packet: GuildNewsChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildMessageChannelData<GuildNewsChannelPacket, GuildNewsChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildNewsChannel(this) }
    private val messages = LruWeakCache<Long, MessageData>()
    override val messageList get() = messages.values
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    override var position = packet.position
        private set
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
        private set
    override var name = packet.name
        private set
    override var isNsfw = packet.nsfw
        private set
    override var parentID = packet.parent_id
        private set
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO.parse(it) }
        private set
    override var topic = packet.topic.orEmpty()
        private set

    override fun update(packet: GuildNewsChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        topic = packet.topic.orEmpty()
        isNsfw = packet.nsfw
        parentID = packet.parent_id
    }

    override fun update(data: ChannelPinsUpdate.Data) {
        data.last_pin_timestamp?.let { lastPinTime = DateFormat.ISO.parse(it) }
    }

    override fun update(data: MessageCreatePacket) = data.toData(this, context).also { messages[it.id] = it }

    override fun getMessageData(id: Long) = messages[id]
}

internal class GuildStoreChannelData(
    packet: GuildStoreChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildStoreChannelPacket, GuildStoreChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildStoreChannel(this) }
    override var position = packet.position
        private set
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
        private set
    override var name = packet.name
        private set
    override var parentID = packet.parent_id
        private set

    override fun update(packet: GuildStoreChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        parentID = packet.parent_id
    }
}

internal class GuildVoiceChannelData(
    packet: GuildVoiceChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildVoiceChannelPacket, GuildVoiceChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildVoiceChannel(this) }
    override var position = packet.position
        private set
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
        private set
    override var name = packet.name
        private set
    override var parentID = packet.parent_id
        private set
    var bitrate = packet.bitrate
        private set
    var userLimit = packet.user_limit
        private set

    override fun update(packet: GuildVoiceChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        parentID = packet.parent_id
        bitrate = packet.bitrate
        userLimit = packet.user_limit
    }
}

internal class GuildChannelCategoryData(
    packet: GuildChannelCategoryPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildChannelCategoryPacket, GuildChannelCategory> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildChannelCategory(this) }
    override var position = packet.position
        private set
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
        private set
    override var name = packet.name
        private set
    override var parentID = packet.parent_id
        private set

    override fun update(packet: GuildChannelCategoryPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        parentID = packet.parent_id
    }
}

/** A private [TextChannelData] open only to the Bot and a single non-bot User. */
internal class DmChannelData(packet: DmChannelPacket, override val context: BotClient) :
    TextChannelData<DmChannelPacket, DmChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { DmChannel(this) }
    private val messages = LruWeakCache<Long, MessageData>()
    override val messageList get() = messages.values
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO.parse(it) }
        private set
    var recipient = packet.recipients.firstOrNull()?.let { context.cache.pullUserData(it) }
        private set

    override fun update(packet: DmChannelPacket) {
        recipient = packet.recipients.firstOrNull()?.let { context.cache.pullUserData(it) }
    }

    override fun update(data: ChannelPinsUpdate.Data) {
        data.last_pin_timestamp?.let { lastPinTime = DateFormat.ISO.parse(it) }
    }

    override fun update(data: MessageCreatePacket) = data.toData(this, context).also { messages[it.id] = it }

    override fun getMessageData(id: Long) = messages[id]
}

internal suspend fun ChannelPacket.toData(context: BotClient) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GuildChannelPacket -> toGuildChannelData(context.cache.getGuildData(guild_id!!)!!, context) //TODO guild_id nulls
    else -> throw IllegalStateException("Attempted to convert an unknown ChannelPacket type to ChannelData.")
}

internal suspend fun TextChannelPacket.toData(context: BotClient) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GuildTextChannelPacket -> toGuildTextChannelData(context.cache.getGuildData(guild_id!!)!!, context)
    else -> throw IllegalStateException("Attempted to convert an unknown TextChannelPacket type to TextChannelData.")
}

internal fun GuildChannelPacket.toGuildChannelData(guildData: GuildData, context: BotClient) = when (this) {
    is GuildTextChannelPacket -> toGuildTextChannelData(guildData, context)
    is GuildNewsChannelPacket -> toGuildNewsChannelData(guildData, context)
    is GuildStoreChannelPacket -> toGuildStoreChannelData(guildData, context)
    is GuildVoiceChannelPacket -> toGuildVoiceChannelData(guildData, context)
    is GuildChannelCategoryPacket -> toGuildChannelCategoryData(guildData, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}

internal fun GuildTextChannelPacket.toGuildTextChannelData(guildData: GuildData, context: BotClient) =
    GuildTextChannelData(this, guildData, context)

internal fun GuildNewsChannelPacket.toGuildNewsChannelData(guildData: GuildData, context: BotClient) =
    GuildNewsChannelData(this, guildData, context)

internal fun GuildStoreChannelPacket.toGuildStoreChannelData(guildData: GuildData, context: BotClient) =
    GuildStoreChannelData(this, guildData, context)

internal fun GuildVoiceChannelPacket.toGuildVoiceChannelData(guildData: GuildData, context: BotClient) =
    GuildVoiceChannelData(this, guildData, context)

internal fun GuildChannelCategoryPacket.toGuildChannelCategoryData(guildData: GuildData, context: BotClient) =
    GuildChannelCategoryData(this, guildData, context)

internal fun DmChannelPacket.toDmChannelData(context: BotClient) = DmChannelData(this, context)
