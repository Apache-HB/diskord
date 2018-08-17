package com.serebit.diskord.internal.network.payloads

internal data class HeartbeatPayload(val d: Int?) : Payload(opcode) {
    companion object {
        const val opcode = 1
    }
}
