package com.serebit.diskord.internal

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

internal object JSON {
    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
        registerModule(KotlinModule())
    }

    inline fun <reified T : Any> parse(json: String): T = objectMapper.readValue(json)

    fun stringify(src: Any): String = objectMapper.writeValueAsString(src)
}
