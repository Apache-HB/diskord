package com.serebit.diskord.internal.network.endpoints

import org.http4k.core.Method

internal abstract class Endpoint<T : Any>(val method: Method, val path: String, vararg val majorParameters: Long) {
    val uri get() = "$baseUri$path"

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    }
}
