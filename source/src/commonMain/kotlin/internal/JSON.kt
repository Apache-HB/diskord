package com.serebit.diskord.internal

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

internal expect object JSON {
    inline fun <reified T : Any> parse(serializer: KSerializer<T>, json: String): T

    fun <T : Any> parse(json: String, type: KClass<T>): T

    fun <T : Any> stringify(serializer: KSerializer<T>, src: T): String
}
