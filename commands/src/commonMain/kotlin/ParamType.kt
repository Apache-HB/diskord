package com.serebit.strife.commands

import kotlin.reflect.KClass

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
            val typeAssociations = mapOf(
                Byte::class to ByteParam,
                Short::class to ShortParam,
                Int::class to IntParam,
                Long::class to LongParam,
                Float::class to FloatParam,
                Double::class to DoubleParam
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
            val typeAssociations = mapOf(
                String::class to StringParam,
                Boolean::class to BooleanParam,
                Char::class to CharParam
            )
        }
    }

    companion object {
        operator fun invoke(type: KClass<out Any>): ParamType<*>? = when (type) {
            in NumberType.typeAssociations -> NumberType.typeAssociations[type]
            in OtherType.typeAssociations -> OtherType.typeAssociations[type]
            else -> null
        }
    }
}

internal val List<ParamType<*>>.signature
    get() = if (isEmpty()) "" else joinToString(" ") { "(${it.signature.pattern})" }
