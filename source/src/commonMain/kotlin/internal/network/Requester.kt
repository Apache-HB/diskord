package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.network.endpoints.Endpoint
import com.serebit.diskord.internal.payloads.IdentifyPayload
import io.ktor.client.response.HttpResponse

internal expect object Requester {
    val identification: IdentifyPayload.Data

    fun initialize(token: String)

    inline fun <reified T : Any> requestObject(
        endpoint: Endpoint<T>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): T?

    fun requestResponse(
        endpoint: Endpoint<out Any>,
        params: Map<String, String> = mapOf(),
        data: Any? = null
    ): HttpResponse
}
