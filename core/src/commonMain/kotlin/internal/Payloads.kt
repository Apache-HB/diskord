package com.serebit.strife.internal

import com.serebit.strife.Context
import com.serebit.strife.data.UnknownOpcodeException
import com.serebit.strife.events.Event
import com.serebit.strife.internal.dispatches.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int

private object Opcodes {
    const val DISPATCH = 0
    const val HEARTBEAT = 1
    const val IDENTIFY = 2
    const val RESUME = 6
    const val HELLO = 10
    const val HEARTBEAT_ACK = 11
}

@Serializable
internal sealed class Payload(val op: Int) {
    companion object {
        fun from(json: String) = when (val opcode = Json.nonstrict.parseJson(json).jsonObject["op"].int) {
            Opcodes.DISPATCH -> DispatchPayload.from(json)
            Opcodes.HELLO -> Json.nonstrict.parse(HelloPayload.serializer(), json)
            Opcodes.IDENTIFY -> Json.nonstrict.parse(IdentifyPayload.serializer(), json)
            Opcodes.RESUME -> Json.nonstrict.parse(ResumePayload.serializer(), json)
            Opcodes.HEARTBEAT -> Json.nonstrict.parse(HeartbeatPayload.serializer(), json)
            Opcodes.HEARTBEAT_ACK -> Json.nonstrict.parse(HeartbeatAckPayload.serializer(), json)
            else -> throw UnknownOpcodeException("Received a payload with an unknown opcode of $opcode.")
        }
    }
}

internal abstract class DispatchPayload : Payload(Opcodes.DISPATCH) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    companion object {
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
            val type = Json.nonstrict.parseJson(json).jsonObject["t"].content
            return Json.nonstrict.parse(dispatchTypeAssociations[type] ?: Unknown.serializer(), json)
        }
    }
}

@Serializable
internal class HeartbeatAckPayload : Payload(Opcodes.HEARTBEAT_ACK)

@Serializable
internal data class HeartbeatPayload(val d: Int?) : Payload(Opcodes.HEARTBEAT)

@Serializable
internal data class HelloPayload(val d: Data) : Payload(Opcodes.HELLO) {
    @Serializable
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)
}

@Serializable
internal data class IdentifyPayload(val d: Data) : Payload(Opcodes.IDENTIFY) {
    @Serializable
    data class Data(val token: String, val properties: Map<String, String>)
}

@Serializable
internal data class ResumePayload(val d: Data) : Payload(Opcodes.RESUME) {
    @Serializable
    data class Data(val token: String, val session_id: String, val seq: Int)

    constructor(token: String, session_id: String, seq: Int) : this(Data(token, session_id, seq))
}
