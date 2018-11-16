package com.serebit.diskord.internal.network.endpoints

import io.ktor.http.HttpMethod
import kotlinx.serialization.KSerializer

internal sealed class Endpoint {
    abstract val method: HttpMethod
    abstract val path: String
    abstract val majorParameters: LongArray
    val uri get() = "$baseUri$path"

    abstract class Response(
        override val method: HttpMethod,
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint()

    abstract class Object<T>(
        override val method: HttpMethod,
        override val path: String,
        val serializer: KSerializer<T>,
        override vararg val majorParameters: Long
    ) : Endpoint()

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "http://discordapp.com/api/v$apiVersion/"
    }
}
