package com.serebit.strife.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.util.*

internal actual fun newRequestHandler() = HttpClient(OkHttp)

internal actual fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
