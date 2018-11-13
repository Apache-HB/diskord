package com.serebit.diskord.internal.payloads

import kotlinx.serialization.Serializable

@Serializable
internal data class IdentifyPayload(val d: Data) : Payload(opcode) {
    data class Data(val token: String, val properties: Map<String, String>)

    companion object {
        const val opcode = 2
    }
}
