package com.serebit.diskord.network.payloads

import com.serebit.diskord.Serializer
import com.serebit.loggerkt.Logger
import org.json.JSONObject

internal abstract class Payload(val op: Int) {
    companion object {
        fun from(json: String): Payload? {
            val opcode = JSONObject(json)["op"] as Int
            return when (opcode) {
                DispatchPayload.opcode -> DispatchPayload.from(json)
                HelloPayload.opcode -> Serializer.fromJson<HelloPayload>(json)
                IdentifyPayload.opcode -> Serializer.fromJson<IdentifyPayload>(json)
                ResumePayload.opcode -> Serializer.fromJson<ResumePayload>(json)
                HeartbeatPayload.opcode -> Serializer.fromJson<HeartbeatPayload>(json)
                HeartbeatAckPayload.opcode -> Serializer.fromJson<HeartbeatAckPayload>(json)
                else -> {
                    Logger.warn("Unknown opcode $opcode received. Ignoring payload.")
                    null
                }
            }
        }
    }
}
