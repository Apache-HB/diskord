package com.serebit.diskord.internal.network.endpoints

import io.ktor.http.HttpMethod

internal object GetGateway : Endpoint.NoData(HttpMethod.Get, "gateway")

internal object GetGatewayBot : Endpoint.NoData(HttpMethod.Get, "gateway/bot")
