package com.serebit.diskord.network.payloads

internal data class ResumePayload(val d: Data) : Payload(opcode) {
    data class Data(val token: String, val session_id: String, val seq: Int)

    companion object {
        const val opcode = 6
    }
}
