package com.serebit.diskord.internal

import kotlin.reflect.KClass

internal expect object JSON {
    inline fun <reified T : Any> parse(json: String): T

    fun <T : Any> parse(json: String, type: KClass<T>): T

    fun stringify(src: Any): String
}
