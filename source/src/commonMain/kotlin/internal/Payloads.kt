package com.serebit.strife.internal

import com.serebit.strife.Context
import com.serebit.strife.data.UnknownOpcodeException
import com.serebit.strife.events.Event
import com.serebit.strife.internal.dispatches.ChannelCreate
import com.serebit.strife.internal.dispatches.ChannelDelete
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.ChannelUpdate
import com.serebit.strife.internal.dispatches.GuildCreate
import com.serebit.strife.internal.dispatches.GuildDelete
import com.serebit.strife.internal.dispatches.GuildUpdate
import com.serebit.strife.internal.dispatches.MessageCreate
import com.serebit.strife.internal.dispatches.MessageDelete
import com.serebit.strife.internal.dispatches.MessageUpdate
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.dispatches.Unknown
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal sealed class Payload(val op: Int) {
    @Serializable
    private data class BasicPayload(val op: Int)

    companion object {
        fun from(json: String): Payload? =
            when (val opcode = Json.nonstrict.parse(BasicPayload.serializer(), json).op) {
                DispatchPayload.opcode -> DispatchPayload.from(json)
                HelloPayload.opcode -> Json.nonstrict.parse(HelloPayload.serializer(), json)
                IdentifyPayload.opcode -> Json.nonstrict.parse(IdentifyPayload.serializer(), json)
                ResumePayload.opcode -> Json.nonstrict.parse(ResumePayload.serializer(), json)
                HeartbeatPayload.opcode -> Json.nonstrict.parse(HeartbeatPayload.serializer(), json)
                HeartbeatAckPayload.opcode -> Json.nonstrict.parse(HeartbeatAckPayload.serializer(), json)
                else -> throw UnknownOpcodeException("Received a payload with an unknown opcode of $opcode.")
            }
    }
}

internal abstract class DispatchPayload : Payload(opcode) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    @Serializable
    private data class BasicDispatch(val t: String)

    companion object {
        const val opcode = 0

        private val dispatchTypeAssociations: Map<String, KSerializer<out DispatchPayload>> = mapOf(
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
            val type = Json.nonstrict.parse(BasicDispatch.serializer(), json).t
            return Json.nonstrict.parse(dispatchTypeAssociations[type] ?: Unknown.serializer(), json)
        }
    }
}

@Serializable
internal class HeartbeatAckPayload : Payload(opcode) {
    companion object {
        const val opcode = 11
    }
}

@Serializable
internal data class HeartbeatPayload(val d: Int?) : Payload(opcode) {
    companion object {
        const val opcode = 1
    }
}

@Serializable
internal data class HelloPayload(val d: Data) : Payload(opcode) {
    @Serializable
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)

    companion object {
        const val opcode = 10
    }
}

@Serializable
internal data class IdentifyPayload(val d: Data) : Payload(opcode) {
    @Serializable
    data class Data(val token: String, val properties: Map<String, String>)

    companion object {
        const val opcode = 2
    }
}

@Serializable
internal data class ResumePayload(val d: Data) : Payload(opcode) {
    @Serializable
    data class Data(val token: String, val session_id: String, val seq: Int)

    constructor(token: String, session_id: String, seq: Int) : this(Data(token, session_id, seq))

    companion object {
        const val opcode = 6
    }
}
