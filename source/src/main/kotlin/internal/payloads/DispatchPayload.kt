package com.serebit.diskord.internal.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.events.Event
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.payloads.dispatches.*

internal abstract class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    private data class BasicDispatch(val t: String)

    companion object {
        const val opcode = 0

        fun from(json: String): DispatchPayload {
            val type = JSON.parse<BasicDispatch>(json).t
            return when (type) {
                "READY" -> JSON.parse<Ready>(json)
                "GUILD_CREATE" -> JSON.parse<GuildCreate>(json)
                "GUILD_UPDATE" -> JSON.parse<GuildUpdate>(json)
                "GUILD_DELETE" -> JSON.parse<GuildDelete>(json)
                "CHANNEL_CREATE" -> JSON.parse<ChannelCreate>(json)
                "CHANNEL_UPDATE" -> JSON.parse<ChannelUpdate>(json)
                "CHANNEL_DELETE" -> JSON.parse<ChannelDelete>(json)
                "CHANNEL_PINS_UPDATE" -> JSON.parse<ChannelPinsUpdate>(json)
                "MESSAGE_CREATE" -> JSON.parse<MessageCreate>(json)
                "MESSAGE_UPDATE" -> JSON.parse<MessageUpdate>(json)
                "MESSAGE_DELETE" -> JSON.parse<MessageDelete>(json)
                "TYPING_START" -> JSON.parse<TypingStart>(json)
                else -> JSON.parse<Unknown>(json)
            }
        }
    }
}
