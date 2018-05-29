package com.serebit.diskord

import com.github.salomonbrys.kotson.gsonTypeToken
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.serebit.diskord.data.ChannelData
import com.serebit.diskord.gateway.Payload
import java.lang.reflect.Type

internal object Serializer {
    private val gson: Gson = GsonBuilder().apply {
        registerTypeAdapter(Payload.Dispatch.deserializer)
        registerTypeAdapter(ChannelData.deserializer)
    }.create()

    inline fun <reified T : Any> fromJson(json: String) = fromJson<T>(json, gsonTypeToken<T>())

    fun <T : Any> fromJson(json: String, type: Type): T = gson.fromJson(json, type)
}
