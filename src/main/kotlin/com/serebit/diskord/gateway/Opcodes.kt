package com.serebit.diskord.gateway

object Opcodes {
    const val dispatch = 0
    const val heartbeat = 1
    const val identify = 2
    const val statusUpdate = 3
    const val voiceStatusUpdate = 4
    const val voiceServerPing = 5
    const val resume = 6
    const val reconnect = 7
    const val requestGuildMembers = 8
    const val invalidSession = 9
    const val hello = 10
    const val heartbeatAck = 11
}
