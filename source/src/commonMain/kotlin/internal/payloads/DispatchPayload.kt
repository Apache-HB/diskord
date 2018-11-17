package com.serebit.diskord.internal.payloads

import com.serebit.diskord.Context
import com.serebit.diskord.events.Event
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
import kotlinx.serialization.json.JSON

internal abstract class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    @Serializable
    private data class BasicDispatch(val t: String)

    companion object {
        const val opcode = 0

        private val dispatchTypeAssociations = mapOf(
            "READY" to Ready.serializer(),
            "GUILD_CREATE" to GuildCreate.serializer(),
            "GUILD_UPDATE" to GuildUpdate.serializer(),
            "GUILD_DELETE" to GuildDelete.serializer(),
            "CHANNEL_CREATE" to ChannelCreate.serializer(),
            "CHANNEL_UPDATE" to ChannelUpdate.serializer(),
            "CHANNEL_DELETE" to ChannelDelete.serializer(),
            "CHANNEL_PINS_UPDATE" to ChannelPinsUpdate.serializer(),
            "MESSAGE_CREATE" to MessageCreate.serializer(),
            "MESSAGE_UPDATE" to MessageUpdate.serializer(),
            "MESSAGE_DELETE" to MessageDelete.serializer(),
            "TYPING_START" to TypingStart.serializer()
        )

        fun from(json: String): DispatchPayload {
            val type = JSON.nonstrict.parse(BasicDispatch.serializer(), json).t
            return JSON.nonstrict.parse(dispatchTypeAssociations[type] ?: Unknown.serializer(), json)
        }
    }
}
