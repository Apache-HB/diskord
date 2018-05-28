package com.serebit.diskord.entities

import com.serebit.diskord.requester

sealed class Channel {
    abstract val id: Long

    fun send(message: String) {
        val response = requester.post("/channels/$id/messages", data = mapOf("content" to message))
        println(response.text)
    }
}

data class GuildTextChannel internal constructor(
    override val id: Long,
    val name: String,
    val topic: String,
    val position: Int,
    val nsfw: Boolean
) : Channel()
