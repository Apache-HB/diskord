package com.serebit.diskord.internal.network.endpoints

import org.http4k.core.Method

internal sealed class Endpoint<T : Any> {
    abstract val path: String
    abstract val majorParameters: LongArray
    abstract val method: Method
    val uri get() = "$baseUri$path"

    internal abstract class Get<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>() {
        override val method = Method.GET
    }

    internal abstract class Post<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>() {
        override val method = Method.POST
    }

    internal abstract class Put<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>() {
        override val method = Method.PUT
    }

    internal abstract class Patch<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>() {
        override val method = Method.PATCH
    }

    internal abstract class Delete<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>() {
        override val method = Method.DELETE
    }

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    }
}
