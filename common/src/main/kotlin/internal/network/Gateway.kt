package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.payloads.HelloPayload
import com.serebit.diskord.internal.payloads.dispatches.Ready

internal expect class Gateway(uri: String, token: String, eventDispatcher: EventDispatcher) {
    fun connect(): HelloPayload?

    fun disconnect()

    fun openSession(hello: HelloPayload): Ready?
}
