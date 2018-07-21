package com.serebit.diskord.network.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.Serializer
import com.serebit.diskord.packets.UnavailableGuildPacket
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.events.*
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.ReadyEvent
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
            val private_channels: List<Channel>,
            val guilds: List<UnavailableGuildPacket>,
            val _trace: List<String>
        )

        companion object {
            const val type = "READY"
        }
    }

    class GuildCreate(override val s: Int, override val d: Guild) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = GuildCreatedEvent(context, d)

        companion object {
            const val type = "GUILD_CREATE"
        }
    }

    class MessageCreate(override val s: Int, override val d: Message) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)

        companion object {
            const val type = "MESSAGE_CREATE"
        }
    }

    class ChannelCreate(override val s: Int, override val d: Channel) : DispatchPayload() {
        override suspend fun asEvent(context: Context) = ChannelCreatedEvent(context, d)

        companion object {
            const val type = "CHANNEL_CREATE"
        }
    }

    class Unknown(override val s: Int, val t: String, override val d: Any) : DispatchPayload() {
        override suspend fun asEvent(context: Context): Event? = null
    }

    companion object {
        const val opcode = 0

        fun from(json: String): DispatchPayload {
            val type = JSONObject(json)["t"] as String
            return when (type) {
                Ready.type -> Serializer.fromJson<Ready>(json)
                GuildCreate.type -> Serializer.fromJson<GuildCreate>(json)
                ChannelCreate.type -> Serializer.fromJson<ChannelCreate>(json)
                MessageCreate.type -> Serializer.fromJson<MessageCreate>(json)
                else -> Serializer.fromJson<Unknown>(json)
            }
        }
    }
}
