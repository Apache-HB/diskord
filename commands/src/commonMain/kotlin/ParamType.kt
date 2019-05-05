package com.serebit.strife.commands

import kotlin.reflect.KClass

internal sealed class ParamType(val name: String, val signature: Regex) {
    sealed class NumberType(name: String, signature: Regex) : ParamType(name, signature) {
        object ByteParam : NumberType("Byte", "[+-]?\\d{1,3}?".toRegex())
        object ShortParam : NumberType("Short", "[+-]?\\d{1,5}?".toRegex())
        object IntParam : NumberType("Int", "[+-]?\\d{1,10}?".toRegex())
        object LongParam : NumberType("Long", "[+-]?\\d{1,19}?L?".toRegex())
        object FloatParam : NumberType("Float", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?[fF]?".toRegex())
        object DoubleParam : NumberType("Double", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?".toRegex())

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

    sealed class OtherType(name: String, signature: Regex) : ParamType(name, signature) {
        object StringParam : OtherType("String", "\\S+".toRegex())
        object BooleanParam : OtherType("Boolean", "true|false".toRegex())
        object CharParam : OtherType("Char", "\\S".toRegex())

        companion object {
            val typeAssociations = mapOf(
                String::class to StringParam,
                Boolean::class to BooleanParam,
                Char::class to CharParam
            )
        }
    }

    companion object {
        operator fun invoke(type: KClass<out Any>): ParamType? = when (type) {
            in NumberType.typeAssociations -> NumberType.typeAssociations[type]
            in OtherType.typeAssociations -> OtherType.typeAssociations[type]
            else -> null
        }
    }
}

internal val List<ParamType>.signature
    get() = if (isEmpty()) "" else joinToString(" ") { "(${it.signature.pattern})" }
