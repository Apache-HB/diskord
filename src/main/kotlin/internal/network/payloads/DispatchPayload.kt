package com.serebit.diskord.internal.network.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.events.ChannelCreatedEvent
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.ReadyEvent
import com.serebit.diskord.events.TypingStartEvent
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket
import org.json.JSONObject

internal sealed class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    class Ready(override val s: Int, override val d: Data) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = ReadyEvent(context, d.user)

        data class Data(
            val v: Int,
            val user: User,
            val private_channels: List<DmChannelPacket>,
            val guilds: List<UnavailableGuildPacket>,
            val _trace: List<String>
        )
    }

    class GuildCreate(override val s: Int, override val d: GuildPacket) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = GuildCreatedEvent(context, d)
    }

    class MessageCreate(override val s: Int, override val d: Message) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)
    }

    class ChannelCreate(override val s: Int, override val d: Channel) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = ChannelCreatedEvent(context, d)
    }

    class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
        override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, this)

        data class Data(val channel_id: Snowflake, val user_id: Snowflake, val timestamp: UnixTimestamp)
    }

    class Unknown(override val s: Int, val t: String, override val d: Any) : DispatchPayload() {
        override suspend fun asEvent(context: Context): Event? = null
    }

    companion object {
        const val opcode = 0

        fun from(json: String): DispatchPayload {
            val type = JSONObject(json)["t"] as String
            return when (type) {
                "READY" -> JSON.parse<Ready>(json)
                "GUILD_CREATE" -> JSON.parse<GuildCreate>(json)
                "CHANNEL_CREATE" -> JSON.parse<ChannelCreate>(json)
                "MESSAGE_CREATE" -> JSON.parse<MessageCreate>(json)
                "TYPING_START" -> JSON.parse<TypingStart>(json)
                else -> JSON.parse<Unknown>(json)
            }
        }
    }
}
