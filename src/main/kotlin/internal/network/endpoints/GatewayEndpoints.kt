package com.serebit.diskord.internal.network.endpoints

import org.http4k.core.Response

internal object GetGateway : Endpoint.Get<Response>("/gateway")

internal object GetGatewayBot : Endpoint.Get<Response>("/gateway/bot")
