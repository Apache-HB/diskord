package com.serebit.diskord.internal.payloads

import kotlinx.serialization.Serializable

@Serializable
internal data class ResumePayload(val d: Data) : Payload(opcode) {
    @Serializable
    data class Data(val token: String, val session_id: String, val seq: Int)

    constructor(token: String, session_id: String, seq: Int) : this(Data(token, session_id, seq))

    companion object {
        const val opcode = 6
    }
}
