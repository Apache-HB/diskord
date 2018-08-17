package com.serebit.diskord.internal.network.payloads

internal data class IdentifyPayload(val d: Data) : Payload(opcode) {
    data class Data(val token: String, val properties: Map<String, String>)

    companion object {
        const val opcode = 2
    }
}
