package com.serebit.diskord.network.endpoints

import com.serebit.diskord.entities.channels.Channel as DiskordChannel

internal sealed class Endpoint<T : Any> {
    abstract val path: String
    abstract val majorParameters: LongArray
    val uri get() = "$baseUri$path"

    internal abstract class Get<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>()

    internal abstract class Post<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>()

    internal abstract class Put<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>()

    internal abstract class Patch<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>()

    internal abstract class Delete<T : Any>(
        override val path: String,
        override vararg val majorParameters: Long
    ) : Endpoint<T>()

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    }
}
