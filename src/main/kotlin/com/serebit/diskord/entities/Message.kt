package com.serebit.diskord.entities

data class Message(
    override val id: Long,
    val channel: TextChannel,
    val author: User,
    val content: String
) : DiscordEntity
