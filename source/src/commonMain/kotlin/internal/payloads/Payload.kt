package com.serebit.diskord.internal.payloads

import com.serebit.diskord.data.UnknownOpcodeException
import com.serebit.diskord.internal.JSON
import kotlinx.serialization.Serializable

@Serializable
internal abstract class Payload(val op: Int) {
    @Serializable
    private data class BasicPayload(val op: Int)

    companion object {
        fun from(json: String): Payload? = when (val opcode = JSON.parse(BasicPayload.serializer(), json).op) {
            DispatchPayload.opcode -> DispatchPayload.from(json)
            HelloPayload.opcode -> JSON.parse(HelloPayload.serializer(), json)
            IdentifyPayload.opcode -> JSON.parse(IdentifyPayload.serializer(), json)
            ResumePayload.opcode -> JSON.parse(ResumePayload.serializer(), json)
            HeartbeatPayload.opcode -> JSON.parse(HeartbeatPayload.serializer(), json)
            HeartbeatAckPayload.opcode -> JSON.parse(HeartbeatAckPayload.serializer(), json)
            else -> throw UnknownOpcodeException("Received a payload with an unknown opcode of $opcode.")
        }
    }
}
