package com.serebit.diskord.entities

import com.serebit.diskord.requester

interface Channel {
    val id: Long
}

class GuildTextChannel internal constructor(
    override val id: Long,
    val guildId: Long,
    val name: String,
    val position: Int,
    val nsfw: Boolean
) : Channel {
    fun send(message: String) = requester.post("/channels/$id/messages", mapOf("content" to message))
}
