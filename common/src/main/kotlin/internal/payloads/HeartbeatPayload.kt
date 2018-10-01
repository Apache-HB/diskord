package com.serebit.diskord.internal.payloads

internal data class HeartbeatPayload(val d: Int?) : Payload(opcode) {
    companion object {
        const val opcode = 1
    }
}
