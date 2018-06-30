package com.serebit.diskord.network

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.serebit.diskord.Context
import com.serebit.diskord.data.UnavailableGuild
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.events.ChannelCreatedEvent
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.ReadyEvent

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, property = "op")
internal sealed class Payload {
    abstract val op: Int

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "t")
    @JsonSubTypes(
        JsonSubTypes.Type(Dispatch.Ready::class, name = "READY"),
        JsonSubTypes.Type(Dispatch.ChannelCreate::class, name = "CHANNEL_CREATE"),
        JsonSubTypes.Type(Dispatch.GuildCreate::class, name = "GUILD_CREATE"),
        JsonSubTypes.Type(Dispatch.MessageCreate::class, name = "MESSAGE_CREATE")
    )
    sealed class Dispatch : Payload() {
        override val op = GatewayOpcodes.dispatch
        abstract val d: Any

        abstract suspend fun asEvent(context: Context): Event?

        class Ready(override val d: Data) : Dispatch() {
            override suspend fun asEvent(context: Context) = ReadyEvent(context, d.user)

            data class Data(
                val v: Int,
                val user: User,
                val private_channels: List<Channel>,
                val guilds: List<UnavailableGuild>,
                val _trace: List<String>
            )
        }

        class GuildCreate(override val d: Guild) : Dispatch() {
            override suspend fun asEvent(context: Context) = GuildCreatedEvent(context, d)
        }

        class MessageCreate(override val d: Message) : Dispatch() {
            override suspend fun asEvent(context: Context) = MessageCreatedEvent(context, d)
        }

        class ChannelCreate(override val d: Channel) : Dispatch() {
            override suspend fun asEvent(context: Context) = ChannelCreatedEvent(context, d)
        }

        companion object {
            val dispatchTypeNames = setOf("READY", "CHANNEL_CREATE", "GUILD_CREATE", "MESSAGE_CREATE")
        }
    }

    data class Heartbeat(val d: Int?) : Payload() {
        override val op = GatewayOpcodes.heartbeat
    }

    data class Identify(val d: Data) : Payload() {
        override val op = GatewayOpcodes.identify

        data class Data(val token: String, val properties: Map<String, String>)
    }

    data class Resume(val d: Data) : Payload() {
        override val op = GatewayOpcodes.resume

        data class Data(val token: String, val session_id: String, val seq: Int)
    }

    data class Hello(val d: Data) : Payload() {
        override val op = GatewayOpcodes.hello

        data class Data(val heartbeat_interval: Long, val _trace: List<String>)
    }
}
