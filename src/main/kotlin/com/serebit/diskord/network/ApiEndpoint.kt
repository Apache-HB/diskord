package com.serebit.diskord.network

import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.Channel
import khttp.responses.Response

internal sealed class ApiEndpoint<T> {
    abstract val path: String
    abstract val majorParameters: Set<Long>
    val uri get() = "$baseUri$path"

    data class Get<T : Any>(override val path: String, override val majorParameters: Set<Long>) : ApiEndpoint<T>()
    data class Post<T : Any>(override val path: String, override val majorParameters: Set<Long>) : ApiEndpoint<T>()
    data class Put<T : Any>(override val path: String, override val majorParameters: Set<Long>) : ApiEndpoint<T>()

    companion object {
        private const val apiVersion = 6
        private const val baseUri = "https://discordapp.com/api/v$apiVersion"
        val gatewayBot = Get<Response>("/gateway/bot", emptySet())
        fun getChannel(channelId: Long) = Get<Channel>("/channels/$channelId", setOf(channelId))
        fun createMessage(channelId: Long) = Post<Message>("/channels/$channelId/messages", setOf(channelId))
    }
}
