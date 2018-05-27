package com.serebit.diskord

sealed class Payload(val op: Int) {
    data class Heartbeat(val d: Int?) : Payload(1)

    data class Identify(val d: IdentifyData) : Payload(2) {
        data class IdentifyData(val token: String, val properties: Map<String, String>)
    }

    data class Hello(val d: HelloData) : Payload(10) {
        data class HelloData(val heartbeat_interval: Int, val _trace: List<String>)
    }
}
