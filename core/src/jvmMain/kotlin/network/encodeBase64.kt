package com.serebit.strife.internal.network

import java.util.*

internal actual fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
