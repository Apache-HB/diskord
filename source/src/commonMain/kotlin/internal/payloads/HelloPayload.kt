package com.serebit.diskord.internal.payloads

import kotlinx.serialization.Serializable

@Serializable
internal data class HelloPayload(val d: Data) : Payload(opcode) {
    @Serializable
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)

    companion object {
        const val opcode = 10
    }
}
