package com.serebit.diskord.network.endpoints

import com.serebit.diskord.entities.channels.Channel as DiskordChannel

internal sealed class ApiEndpoint<T : Any> {
    abstract val path: String
    abstract val majorParameters: Set<Long>
    val uri get() = "$baseUri$path"

    internal abstract class Get<T : Any>(
        override val path: String,
        override val majorParameters: Set<Long> = emptySet()
    ) : ApiEndpoint<T>()

    internal abstract class Post<T : Any>(
        override val path: String,
        override val majorParameters: Set<Long> = emptySet()
    ) : ApiEndpoint<T>()

    internal abstract class Put<T : Any>(
        override val path: String,
        override val majorParameters: Set<Long> = emptySet()
    ) : ApiEndpoint<T>()

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
    }
}
