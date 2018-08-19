package com.serebit.diskord.internal.network.endpoints

import org.http4k.core.Method
import org.http4k.core.Response

internal object GetGateway : Endpoint<Response>(Method.GET, "/gateway")

internal object GetGatewayBot : Endpoint<Response>(Method.GET, "/gateway/bot")
