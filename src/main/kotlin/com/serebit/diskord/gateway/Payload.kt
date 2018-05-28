package com.serebit.diskord.gateway

import com.serebit.diskord.EntityCacher
import com.serebit.diskord.data.ChannelData
import com.serebit.diskord.data.GuildData
import com.serebit.diskord.data.MessageData
import com.serebit.diskord.data.UserData
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.User
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.GuildCreatedEvent
import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.diskord.events.ReadyEvent

internal sealed class Payload(val op: Int) {
    sealed class Dispatch(val s: Int, val t: String) : Payload(Opcodes.dispatch) {
        abstract val asEvent: Event

        class Ready(s: Int, private val d: Data) : Dispatch(s, "READY") {
            override val asEvent: Event
                get() = ReadyEvent(User(d.user.id.toLong()))

            data class Data(
                val v: Int,
                val user: UserData,
                val private_channels: ChannelData,
                val guilds: GuildData,
                val session_id: String,
                val _trace: List<String>
            )
        }

        class GuildCreate(s: Int, val d: GuildData) : Dispatch(s, "GUILD_CREATE") {
            override val asEvent: Event
                get() = GuildCreatedEvent(Guild(d.id.toLong()))
        }

        class MessageCreate(s: Int, val d: MessageData) : Dispatch(s, "MESSAGE_CREATE") {
            override val asEvent: Event
                get() = MessageCreatedEvent(
                    EntityCacher.channelCache.first { it.id.toString() == d.channel_id },
                    EntityCacher.userCache.first { it.id.toString() == d.author.id }
                )
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
