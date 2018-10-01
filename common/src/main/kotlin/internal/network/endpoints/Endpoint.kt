package com.serebit.diskord.internal.network.endpoints

import io.ktor.http.HttpMethod

internal abstract class Endpoint<T : Any>(val method: HttpMethod, val path: String, vararg val majorParameters: Long) {
    val uri get() = "$baseUri$path"

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    }
}
