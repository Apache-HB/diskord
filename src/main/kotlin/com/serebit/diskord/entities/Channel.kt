package com.serebit.diskord.entities

import com.serebit.diskord.requester

interface Channel : DiscordEntity

interface TextChannel : Channel {
    fun send(message: String) {
        val response = requester.post("/channels/$id/messages", data = mapOf("content" to message))
        println(response.text)
    }
}

interface VoiceChannel : Channel

data class GuildVoiceChannel internal constructor(
    override val id: Long,
    val name: String,
    val position: Int,
    val bitrate: Int,
    val userLimit: Int
) : VoiceChannel

data class GuildTextChannel internal constructor(
    override val id: Long,
    val name: String,
    val topic: String,
    val position: Int,
    val nsfw: Boolean
) : TextChannel

data class PrivateTextChannel internal constructor(
    override val id: Long,
    val recipients: List<User>
) : TextChannel

data class GroupTextChannel internal constructor(
    override val id: Long,
    val icon: String?,
    val recipients: List<User>,
    val owner: User
) : TextChannel

data class ChannelCategory internal constructor(
    override val id: Long,
    val name: String,
    val position: Int
) : Channel

data class UnknownChannel internal constructor(
    override val id: Long
) : Channel
