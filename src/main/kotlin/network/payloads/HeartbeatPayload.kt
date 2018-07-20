package com.serebit.diskord.network.payloads

internal data class HeartbeatPayload(val d: Int?) : Payload(opcode) {
    companion object {
        const val opcode = 1
    }
}
