package com.serebit.diskord.network.payloads

internal class HeartbeatAckPayload : Payload(opcode) {
    companion object {
        const val opcode = 11
    }
}
