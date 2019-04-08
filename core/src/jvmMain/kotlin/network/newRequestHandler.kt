package com.serebit.strife.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun newRequestHandler() = HttpClient(OkHttp)
