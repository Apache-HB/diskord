package com.serebit.diskord.internal.network.endpoints

import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpMethod

internal object GetGateway : Endpoint<HttpResponse>(HttpMethod.Get, "/gateway")

internal object GetGatewayBot : Endpoint<HttpResponse>(HttpMethod.Get, "/gateway/bot")
