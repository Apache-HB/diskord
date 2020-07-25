package com.serebit.strife.internal.dispatches

import com.serebit.strife.events.Event
import com.serebit.strife.internal.DispatchPayload
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal sealed class DispatchConversionResult<T : Event>(val type: KType) {
    class Success<T : Event>(val event: T, type: KType) : DispatchConversionResult<T>(type)
    class Failure<T : Event>(val message: String, type: KType) : DispatchConversionResult<T>(type)
}

@OptIn(ExperimentalStdlibApi::class)
internal inline fun <reified T : Event> DispatchPayload.success(event: T) =
    DispatchConversionResult.Success(event, typeOf<T>())

@OptIn(ExperimentalStdlibApi::class)
internal inline fun <reified T : Event> DispatchPayload.failure(message: String) =
    DispatchConversionResult.Failure<T>(message, typeOf<T>())
