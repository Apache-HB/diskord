package com.serebit.diskord.internal.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.events.Event
import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.payloads.dispatches.ChannelCreate
import com.serebit.diskord.internal.payloads.dispatches.ChannelDelete
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate
import com.serebit.diskord.internal.payloads.dispatches.ChannelUpdate
import com.serebit.diskord.internal.payloads.dispatches.GuildCreate
import com.serebit.diskord.internal.payloads.dispatches.GuildDelete
import com.serebit.diskord.internal.payloads.dispatches.GuildUpdate
import com.serebit.diskord.internal.payloads.dispatches.MessageCreate
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete
import com.serebit.diskord.internal.payloads.dispatches.MessageUpdate
import com.serebit.diskord.internal.payloads.dispatches.Ready
import com.serebit.diskord.internal.payloads.dispatches.TypingStart
import com.serebit.diskord.internal.payloads.dispatches.Unknown
import kotlinx.serialization.Serializable

internal abstract class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    @Serializable
    private data class BasicDispatch(val t: String)

    companion object {
        const val opcode = 0

        private val dispatchTypeAssociations = mapOf(
            "READY" to Ready::class,
            "GUILD_CREATE" to GuildCreate::class,
            "GUILD_UPDATE" to GuildUpdate::class,
            "GUILD_DELETE" to GuildDelete::class,
            "CHANNEL_CREATE" to ChannelCreate::class,
            "CHANNEL_UPDATE" to ChannelUpdate::class,
            "CHANNEL_DELETE" to ChannelDelete::class,
            "CHANNEL_PINS_UPDATE" to ChannelPinsUpdate::class,
            "MESSAGE_CREATE" to MessageCreate::class,
            "MESSAGE_UPDATE" to MessageUpdate::class,
            "MESSAGE_DELETE" to MessageDelete::class,
            "TYPING_START" to TypingStart::class
        )

        fun from(json: String): DispatchPayload {
            val type = JSON.parse(BasicDispatch.serializer(), json).t
            return JSON.parse(json, dispatchTypeAssociations[type] ?: Unknown::class)
        }
    }
}
