package com.serebit.diskord.network.payloads

internal data class HelloPayload(val d: Data) : Payload(opcode) {
    data class Data(val heartbeat_interval: Long, val _trace: List<String>)

    companion object {
        const val opcode = 10
    }
}
