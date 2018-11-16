package com.serebit.diskord.internal

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

internal actual object JSON {
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        registerModule(KotlinModule())
    }

    actual inline fun <reified T : Any> parse(serializer: KSerializer<T>, json: String): T =
        objectMapper.readValue(json)

    actual fun <T : Any> parse(json: String, type: KClass<T>): T = objectMapper.readValue(json, type.java)

    actual fun <T : Any> stringify(serializer: KSerializer<T>, src: T): String = objectMapper.writeValueAsString(src)
}
