package com.serebit.diskord.internal.payloads

import com.serebit.diskord.internal.JSON
import com.serebit.logkat.Logger

internal abstract class Payload(val op: Int) {
    private data class BasicPayload(val op: Int)

    companion object {
        fun from(json: String): Payload? = when (JSON.parse<BasicPayload>(json).op) {
            DispatchPayload.opcode -> DispatchPayload.from(json)
            HelloPayload.opcode -> JSON.parse<HelloPayload>(json)
            IdentifyPayload.opcode -> JSON.parse<IdentifyPayload>(json)
            ResumePayload.opcode -> JSON.parse<ResumePayload>(json)
            HeartbeatPayload.opcode -> JSON.parse<HeartbeatPayload>(json)
            HeartbeatAckPayload.opcode -> JSON.parse<HeartbeatAckPayload>(json)
            else -> {
                Logger.warn("Unknown opcode ${JSON.parse<BasicPayload>(json).op} received. Ignoring payload.")
                null
            }
        }
    }
}
