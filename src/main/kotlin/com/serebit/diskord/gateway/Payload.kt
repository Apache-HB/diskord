package com.serebit.diskord.gateway

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonDeserializer
import com.serebit.diskord.entities.Channel
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.events.ChannelCreatedEvent
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.ReadyEvent

internal sealed class Payload(val op: Int) {
    sealed class Dispatch(val s: Int) : Payload(Opcodes.dispatch) {
        abstract val asEvent: Event?

        class Ready(s: Int, val d: Data) : Dispatch(s) {
            override val asEvent: Event? get() = ReadyEvent(d.user)

            data class Data(
                val v: Int,
                val user: User,
                val private_channels: List<Channel>,
                val guilds: List<Guild>,
                val session_id: String,
                val _trace: List<String>
            )
        }

        class GuildCreate(s: Int, val d: Guild) : Dispatch(s) {
            override val asEvent: Event? get() = GuildCreatedEvent(d)
        }

        class MessageCreate(s: Int, val d: Message) : Dispatch(s) {
            override val asEvent: Event? get() = MessageCreatedEvent(d)
        }

        class ChannelCreate(s: Int, val d: Channel) : Dispatch(s) {
            override val asEvent: Event? get() = ChannelCreatedEvent(d)
        }

        companion object {
            val deserializer = jsonDeserializer { (json, _, context) ->
                when (DispatchType.values().find { it.name == json["t"].asString }) {
                    DispatchType.READY -> context.deserialize<Ready>(json)
                    DispatchType.GUILD_CREATE -> context.deserialize<GuildCreate>(json)
                    DispatchType.CHANNEL_CREATE -> context.deserialize<ChannelCreate>(json)
                    DispatchType.MESSAGE_CREATE -> context.deserialize<MessageCreate>(json)
                    else -> null
                }
            }
        }
    }

    data class Heartbeat(val d: Int?) : Payload(Opcodes.heartbeat)

    data class Identify(val d: Data) : Payload(Opcodes.identify) {
        data class Data(val token: String, val properties: Map<String, String>)
    }

    data class Hello(val d: Data) : Payload(Opcodes.hello) {
        data class Data(val heartbeat_interval: Int, val _trace: List<String>)
    }
}
