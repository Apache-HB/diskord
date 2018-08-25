package com.serebit.diskord.internal.network.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.events.*
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.packets.*

internal sealed class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = ReadyEvent(context, d)

        data class Data(
            val v: Int,
            val user: UserPacket,
            val private_channels: List<DmChannelPacket>,
            val guilds: List<UnavailableGuildPacket>,
            val _trace: List<String>
        )
    }

    class GuildCreate(override val s: Int, override val d: GuildPacket) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = GuildCreatedEvent(context, d)
    }

    class MessageCreate(override val s: Int, override val d: MessagePacket) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)
    }

    class MessageUpdate(override val s: Int, override val d: MessagePacket) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = MessageUpdatedEvent(context, d)
    }

    class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = MessageDeletedEvent(context, d)

        data class Data(val id: Snowflake, val channel_id: Snowflake)
    }

    class ChannelCreate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = ChannelCreatedEvent(context, d)
    }

    class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
        override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, this)

        data class Data(val channel_id: Snowflake, val user_id: Snowflake, val timestamp: UnixTimestamp)
    }

    class Unknown(override val s: Int, val t: String, override val d: Any) : DispatchPayload() {
        override suspend fun asEvent(context: Context): Event? = null
    }

    private data class BasicDispatch(val t: String)

    companion object {
        const val opcode = 0

        fun from(json: String): DispatchPayload {
            val type = JSON.parse<BasicDispatch>(json).t
            return when (type) {
                "READY" -> JSON.parse<Ready>(json)
                "GUILD_CREATE" -> JSON.parse<GuildCreate>(json)
                "CHANNEL_CREATE" -> JSON.parse<ChannelCreate>(json)
                "MESSAGE_CREATE" -> JSON.parse<MessageCreate>(json)
                "MESSAGE_UPDATE" -> JSON.parse<MessageUpdate>(json)
                "MESSAGE_DELETE" -> JSON.parse<MessageDelete>(json)
                "TYPING_START" -> JSON.parse<TypingStart>(json)
                else -> JSON.parse<Unknown>(json)
            }
        }
    }
}
