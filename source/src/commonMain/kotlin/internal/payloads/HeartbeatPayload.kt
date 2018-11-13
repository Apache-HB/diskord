package com.serebit.diskord.internal.payloads

import kotlinx.serialization.Serializable

@Serializable
internal data class HeartbeatPayload(val d: Int?) : Payload(opcode) {
    companion object {
        const val opcode = 1
    }
}
