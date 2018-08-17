package com.serebit.diskord.internal.network.payloads

internal class HeartbeatAckPayload : Payload(opcode) {
    companion object {
        const val opcode = 11
    }
}
