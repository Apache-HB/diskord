package com.serebit.diskord.internal.network.payloads

import com.serebit.diskord.internal.JSON
import com.serebit.loggerkt.Logger
import org.json.JSONObject

internal abstract class Payload(val op: Int) {
    companion object {
        fun from(json: String): Payload? {
            val opcode = JSONObject(json)["op"] as Int
            return when (opcode) {
                DispatchPayload.opcode -> DispatchPayload.from(json)
                HelloPayload.opcode -> JSON.parse<HelloPayload>(json)
                IdentifyPayload.opcode -> JSON.parse<IdentifyPayload>(json)
                ResumePayload.opcode -> JSON.parse<ResumePayload>(json)
                HeartbeatPayload.opcode -> JSON.parse<HeartbeatPayload>(json)
                HeartbeatAckPayload.opcode -> JSON.parse<HeartbeatAckPayload>(json)
                else -> {
                    Logger.warn("Unknown opcode $opcode received. Ignoring payload.")
                    null
                }
            }
        }
    }
}
