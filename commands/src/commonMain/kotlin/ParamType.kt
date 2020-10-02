package com.serebit.strife.commands

import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal sealed class ParamType<T : Any>(val signature: Regex, inline val parse: (String) -> T?) {
    sealed class NumberType<T : Number>(signature: Regex, parse: (String) -> T?) : ParamType<T>(signature, parse) {
        object ByteParam : NumberType<Byte>(
            "[+-]?\\d{1,3}?".toRegex(),
            { it.toByteOrNull() }
        )

        object ShortParam : NumberType<Short>(
            "[+-]?\\d{1,5}?".toRegex(),
            { it.toShortOrNull() }
        )

        object IntParam : NumberType<Int>(
            "[+-]?\\d{1,10}?".toRegex(),
            { it.toIntOrNull() }
        )

        object LongParam : NumberType<Long>(
            "[+-]?\\d{1,19}?L?".toRegex(),
            { it.toLongOrNull() }
        )

        object FloatParam : NumberType<Float>(
            "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?[fF]?".toRegex(),
            { it.toFloatOrNull() }
        )

        object DoubleParam : NumberType<Double>(
            "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?".toRegex(),
            { it.toDoubleOrNull() }
        )

        companion object {
            @OptIn(ExperimentalStdlibApi::class)
            val typeAssociations = mapOf(
                typeOf<Byte>() to ByteParam,
                typeOf<Short>() to ShortParam,
                typeOf<Int>() to IntParam,
                typeOf<Long>() to LongParam,
                typeOf<Float>() to FloatParam,
                typeOf<Double>() to DoubleParam
            )
        }
    }

    sealed class OtherType<T : Any>(signature: Regex, parse: (String) -> T?) : ParamType<T>(signature, parse) {
        object StringParam : OtherType<String>(
            "\\S+".toRegex(),
            { it.takeIf { it.none { char -> char.isWhitespace() } } }
        )

        object BooleanParam : OtherType<Boolean>(
            "true|false".toRegex(),
            { if (it == "true" || it == "false") it.toBoolean() else null }
        )

        object CharParam : OtherType<Char>(
            "\\S".toRegex(),
            { it.singleOrNull() }
        )

        companion object {
            @OptIn(ExperimentalStdlibApi::class)
            val typeAssociations = mapOf(
                typeOf<String>() to StringParam,
                typeOf<Boolean>() to BooleanParam,
                typeOf<Char>() to CharParam
            )
        }
    }

    companion object {
        operator fun invoke(type: KType): ParamType<*>? = when (type) {
            in NumberType.typeAssociations -> NumberType.typeAssociations[type]
            in OtherType.typeAssociations -> OtherType.typeAssociations[type]
            else -> null
        }
    }
}

internal val List<ParamType<*>>.signature
    get() = if (isEmpty()) "" else joinToString(" ") { "(${it.signature.pattern})" }
