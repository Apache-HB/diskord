package com.serebit.diskord.gateway

import com.serebit.diskord.Context
import com.serebit.diskord.data.UnavailableGuild
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
        abstract fun asEvent(context: Context): Event?

        class Ready(s: Int, val d: Data) : Dispatch(s) {
            override fun asEvent(context: Context) = ReadyEvent(context, d.user)

            data class Data(
                val v: Int,
                val user: User,
                val private_channels: List<Channel>,
                val guilds: List<UnavailableGuild>,
                val _trace: List<String>
            )
        }

        class GuildCreate(s: Int, val d: Guild) : Dispatch(s) {
            override fun asEvent(context: Context) = GuildCreatedEvent(context, d)
        }

        class MessageCreate(s: Int, val d: Message) : Dispatch(s) {
            override fun asEvent(context: Context) = MessageCreatedEvent(context, d)
        }

        class ChannelCreate(s: Int, val d: Channel) : Dispatch(s) {
            override fun asEvent(context: Context) = ChannelCreatedEvent(context, d)
        }
    }

    data class Heartbeat(val d: Int?) : Payload(Opcodes.heartbeat)

    data class Identify(val d: Data) : Payload(Opcodes.identify) {
        data class Data(val token: String, val properties: Map<String, String>)
    }

    data class Resume(val d: Data) : Payload(Opcodes.resume) {
        data class Data(val token: String, val session_id: String, val seq: Int)
    }

    data class Hello(val d: Data) : Payload(Opcodes.hello) {
        data class Data(val heartbeat_interval: Int, val _trace: List<String>)
    }
}
