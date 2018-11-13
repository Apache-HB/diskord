package com.serebit.diskord.internal.payloads

import kotlinx.serialization.Serializable

@Serializable
internal class HeartbeatAckPayload : Payload(opcode) {
    companion object {
        const val opcode = 11
    }
}
