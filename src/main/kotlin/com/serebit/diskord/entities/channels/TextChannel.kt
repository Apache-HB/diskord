package com.serebit.diskord.entities.channels

import com.serebit.diskord.network.ApiRequester

interface TextChannel : Channel {
    fun send(message: String) {
        val response = ApiRequester.post("/channels/$id/messages", data = mapOf("content" to message))
        println(response.text)
    }
}
