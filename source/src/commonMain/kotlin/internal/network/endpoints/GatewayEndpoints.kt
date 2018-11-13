package com.serebit.diskord.internal.network.endpoints

import io.ktor.http.HttpMethod

internal object GetGateway : Endpoint.Response(HttpMethod.Get, "gateway")

internal object GetGatewayBot : Endpoint.Response(HttpMethod.Get, "gateway/bot")
