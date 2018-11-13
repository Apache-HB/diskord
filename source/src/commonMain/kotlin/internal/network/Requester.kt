package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.network.endpoints.Endpoint
import com.serebit.diskord.internal.payloads.IdentifyPayload
import io.ktor.client.response.HttpResponse

internal expect object Requester {
    val identification: IdentifyPayload.Data

    fun initialize(token: String)

    inline fun <reified T : Any> requestObject(
        endpoint: Endpoint.Object<T>,
        params: Map<String, String> = mapOf(),
        data: Map<String, String>? = null
    ): T?

    fun requestResponse(
        endpoint: Endpoint.Response,
        params: Map<String, String> = mapOf(),
        data: Map<String, String>? = null
    ): HttpResponse
}
