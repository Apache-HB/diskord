package com.serebit.strife.internal

import com.serebit.strife.Context
import com.serebit.strife.data.UnknownOpcodeException
import com.serebit.strife.events.Event
import com.serebit.strife.events.EventName
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.packets.ActivityPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int

private object Opcodes {
    const val DISPATCH = 0
    const val HEARTBEAT = 1
    const val IDENTIFY = 2
    const val STATUS_UPDATE = 3
    const val VOICE_STATE_UPDATE = 4
    const val RESUME = 6
    const val RECONNECT = 7
    const val REQUEST_GUILD_MEMBERS = 8
    const val INVALID_SESSION = 9
    const val HELLO = 10
    const val HEARTBEAT_ACK = 11
}

@Serializable
internal sealed class Payload(val op: Int) {
    companion object {
        // only includes payloads that can be received from Discord's servers
        @UseExperimental(UnstableDefault::class)
        operator fun invoke(json: String) = when (val opcode = Json.nonstrict.parseJson(json).jsonObject["op"]?.int) {
            Opcodes.DISPATCH -> DispatchPayload(json)
            Opcodes.HEARTBEAT -> Json.nonstrict.parse(HeartbeatPayload.serializer(), json)
            Opcodes.RECONNECT -> Json.nonstrict.parse(ReconnectPayload.serializer(), json)
            Opcodes.INVALID_SESSION -> Json.nonstrict.parse(InvalidSessionPayload.serializer(), json)
            Opcodes.HELLO -> Json.nonstrict.parse(HelloPayload.serializer(), json)
            Opcodes.HEARTBEAT_ACK -> Json.nonstrict.parse(HeartbeatAckPayload.serializer(), json)
            else -> throw UnknownOpcodeException("Received a payload with an invalid opcode of $opcode.")
        }
    }
}

internal abstract class DispatchPayload : Payload(Opcodes.DISPATCH) {
    abstract val d: Any
    abstract val s: Int

    abstract suspend fun asEvent(context: Context): Event?

    companion object {
        @UseExperimental(UnstableDefault::class)
        operator fun invoke(json: String): DispatchPayload {
            val type = Json.nonstrict.parseJson(json).jsonObject["t"]?.content?.let { EventName.byName(it) }
            return Json.nonstrict.parse(type?.serializer ?: Unknown.serializer(), json)
        }
    }
}

@Serializable
internal data class HeartbeatPayload(val d: Int?) : Payload(Opcodes.HEARTBEAT)

@Serializable
internal data class IdentifyPayload(val d: Data) : Payload(Opcodes.IDENTIFY) {
    @Serializable
    data class Data(val token: String, val properties: Map<String, String>)
}

@Serializable
internal class StatusUpdatePayload(val d: Data) : Payload(Opcodes.STATUS_UPDATE) {
    @Serializable
    data class Data(
        val status: String,
        val game: ActivityPacket? = null,
        val afk: Boolean = false,
        val since: Long? = null
    )
}

@Serializable
internal class VoiceStateUpdatePayload : Payload(Opcodes.VOICE_STATE_UPDATE)

@Serializable
internal data class ResumePayload(val d: Data) : Payload(Opcodes.RESUME) {
    @Serializable
    data class Data(val token: String, val session_id: String, val seq: Int)

    constructor(token: String, session_id: String, seq: Int) : this(Data(token, session_id, seq))
}

@Serializable
internal class ReconnectPayload : Payload(Opcodes.RECONNECT)

@Serializable
internal data class RequestGuildMembersPayload(
    val guild_id: Long,
    val query: String,
    val limit: Int
) : Payload(Opcodes.REQUEST_GUILD_MEMBERS)

@Serializable
internal class InvalidSessionPayload : Payload(Opcodes.INVALID_SESSION)

@Serializable
internal data class HelloPayload(val d: Data) : Payload(Opcodes.HELLO) {
    @Serializable
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)
}

@Serializable
internal class HeartbeatAckPayload : Payload(Opcodes.HEARTBEAT_ACK)
