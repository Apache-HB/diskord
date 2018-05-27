package com.serebit.diskord.gateway

import com.serebit.diskord.data.MessageData

sealed class Payload(val op: Int) {
    sealed class Dispatch(val s: Int, val t: String) : Payload(Opcodes.dispatch) {
        class MessageCreate(s: Int, val d: MessageData) : Dispatch(s, "MESSAGE_CREATE")
    }

    data class Heartbeat(val d: Int?) : Payload(Opcodes.heartbeat)

    data class Identify(val d: Data) : Payload(
        Opcodes.identify
    ) {
        data class Data(val token: String, val properties: Map<String, String>)
    }

    data class Hello(val d: Data) : Payload(Opcodes.hello) {
        data class Data(val heartbeat_interval: Int, val _trace: List<String>)
    }
}
