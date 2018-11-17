package com.serebit.diskord.internal.payloads

import com.serebit.diskord.data.UnknownOpcodeException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON

@Serializable
internal abstract class Payload(val op: Int) {
    @Serializable
    private data class BasicPayload(val op: Int)

    companion object {
        fun from(json: String): Payload? = when (val opcode = JSON.nonstrict.parse(BasicPayload.serializer(), json).op) {
            DispatchPayload.opcode -> DispatchPayload.from(json)
            HelloPayload.opcode -> JSON.nonstrict.parse(HelloPayload.serializer(), json)
            IdentifyPayload.opcode -> JSON.nonstrict.parse(IdentifyPayload.serializer(), json)
            ResumePayload.opcode -> JSON.nonstrict.parse(ResumePayload.serializer(), json)
            HeartbeatPayload.opcode -> JSON.nonstrict.parse(HeartbeatPayload.serializer(), json)
            HeartbeatAckPayload.opcode -> JSON.nonstrict.parse(HeartbeatAckPayload.serializer(), json)
            else -> throw UnknownOpcodeException("Received a payload with an unknown opcode of $opcode.")
        }
    }
}
