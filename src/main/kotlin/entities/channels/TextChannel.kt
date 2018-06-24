package com.serebit.diskord.entities.channels

import com.serebit.diskord.network.ApiEndpoint
import com.serebit.diskord.network.ApiRequester
import kotlinx.coroutines.experimental.async

interface TextChannel : Channel {
    fun send(message: String) = async {
        val response = ApiRequester.request(ApiEndpoint.createMessage(id), data = mapOf("content" to message)).await()
        println(response.text)
    }
}
