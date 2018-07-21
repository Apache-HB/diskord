@file:JvmName("GatewayEndpoints")

package com.serebit.diskord.network.endpoints

import khttp.responses.Response

internal object GetGateway : Endpoint.Get<Response>("/gateway")
internal object GetGatewayBot : Endpoint.Get<Response>("/gateway/bot")
