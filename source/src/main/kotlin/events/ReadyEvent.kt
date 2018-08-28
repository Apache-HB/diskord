package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.payloads.DispatchPayload

class ReadyEvent internal constructor(override val context: Context, packet: DispatchPayload.Ready.Data) : Event {
    val user = User(packet.user).cache()
}
