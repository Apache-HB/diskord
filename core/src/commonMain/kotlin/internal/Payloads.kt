package com.serebit.strife.internal

import com.serebit.strife.data.*
import com.serebit.strife.entities.*
import com.serebit.strife.Context
import com.serebit.strife.data.UnknownOpcodeException
import com.serebit.strife.events.Event
import com.serebit.strife.events.EventName
import com.serebit.strife.internal.dispatches.Unknown
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import kotlinx.serialization.json.int

import com.serebit.strife.internal.network.Gateway

/**
 * All [Gateway] events in
 * Discord are tagged with an opcode that denotes the payload type.
 * [see](https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#opcodes-and-status-codes)
 */
private object Opcodes {
    /** [Event] payloads */
    const val DISPATCH = 0
    /** Used for ping checking. */
    const val HEARTBEAT = 1
    /** Used for client handshake. */
    const val IDENTIFY = 2
    /** Used to update the client status. */
    const val STATUS_UPDATE = 3
    /** Used to join/move/leave [voice channels][GuildVoiceChannel]. */
    const val VOICE_STATE_UPDATE = 4
    /** Used to resume a closed connection. */
    const val RESUME = 6
    /** Used to tell clients to reconnect to the [Gateway]. */
    const val RECONNECT = 7
    /** Used to request [guild members][Member]. */
    const val REQUEST_GUILD_MEMBERS = 8
    /** Used to notify client they have an invalid session id. */
    const val INVALID_SESSION = 9
    /** Sent immediately after connecting, contains heartbeat and server debug information. */
    const val HELLO = 10
    /** Sent immediately following a client heartbeat that was received. */
    const val HEARTBEAT_ACK = 11
}

/**
 * Packets sent from the the Discord API to the [Gateway] are encapsulated within a [Payload] with the proper [Opcodes]
 * and data object set. Payloads to the gateway are limited to a maximum of 4096 bytes sent, going over this will cause
 * a connection termination with error code 4002.
 *
 * [see](https://discordapp.com/developers/docs/topics/gateway#payloads)
 */
@Serializable
internal sealed class Payload(val op: Int) {
    companion object {
        // only includes payloads that can be received from Discord's servers
        fun from(json: String) = when (val opcode = Json.nonstrict.parseJson(json).jsonObject["op"]?.int) {
            Opcodes.DISPATCH -> DispatchPayload.from(json)
            Opcodes.HEARTBEAT -> Json.nonstrict.parse(HeartbeatPayload.serializer(), json)
            Opcodes.RECONNECT -> Json.nonstrict.parse(ReconnectPayload.serializer(), json)
            Opcodes.INVALID_SESSION -> Json.nonstrict.parse(InvalidSessionPayload.serializer(), json)
            Opcodes.HELLO -> Json.nonstrict.parse(HelloPayload.serializer(), json)
            Opcodes.HEARTBEAT_ACK -> Json.nonstrict.parse(HeartbeatAckPayload.serializer(), json)
            else -> throw UnknownOpcodeException("Received a payload with an invalid opcode of $opcode.")
        }
    }
}

/** A [Payload] used for sending [Event] data through the [Gateway]. */
internal abstract class DispatchPayload : Payload(Opcodes.DISPATCH) {
    /** The [Event] data of this [Payload]. */
    abstract val d: Any
    /** Sequence number used for resuming sessions and heartbeats. */
    abstract val s: Int

    /** Get this [DispatchPayload] as an [Event]. */
    abstract suspend fun asEvent(context: Context): Event?

    companion object {
        /** Parse a [DispatchPayload] from a [Json] String. */
        fun from(json: String): DispatchPayload {
            val type = Json.nonstrict.parseJson(json).jsonObject["t"]?.content?.let { EventName.byName(it) }
            return Json.nonstrict.parse(type?.serializer ?: Unknown.serializer(), json)
        }
    }
}

/** [see](https://discordapp.com/developers/docs/topics/gateway#heartbeating) */
@Serializable
internal data class HeartbeatPayload(val d: Int?) : Payload(Opcodes.HEARTBEAT)

/** [see](https://discordapp.com/developers/docs/topics/gateway#heartbeating) */
@Serializable
internal data class IdentifyPayload(val d: Data) : Payload(Opcodes.IDENTIFY) {
    @Serializable
    data class Data(val token: String, val properties: Map<String, String>)
}

@Serializable
internal class StatusUpdatePayload : Payload(Opcodes.STATUS_UPDATE)

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
internal data class RequestGuildMembersPayload(val guild_id: Long, val query: String, val limit: Int) :
    Payload(Opcodes.REQUEST_GUILD_MEMBERS)

@Serializable
internal class InvalidSessionPayload : Payload(Opcodes.INVALID_SESSION)

@Serializable
internal data class HelloPayload(val d: Data) : Payload(Opcodes.HELLO) {
    @Serializable
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)
}

@Serializable
internal class HeartbeatAckPayload : Payload(Opcodes.HEARTBEAT_ACK)
