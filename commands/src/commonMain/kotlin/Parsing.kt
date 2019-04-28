package com.serebit.strife.commands

import kotlin.reflect.KClass

internal class Parser {
    fun tokenize(message: String, signature: Regex): List<String>? =
        signature.find(message)?.groups?.mapNotNull { it?.value }?.drop(1)

    fun parseTokensOrNull(tokens: List<String>, tokenTypes: List<ParamType>): List<Any>? =
        if (tokens.isNotEmpty()) castTokens(tokenTypes, tokens.takeLast(tokenTypes.size)) else null

    private fun castTokens(types: List<ParamType>, tokens: List<String>): List<Any>? {
        val castedTokens = types.zip(tokens).map { (type, token) ->
            when (type) {
                is ParamType.Number -> castNumeral(type, token)
                is ParamType.Other -> castOther(type, token)
            }
        }
        return if (null in castedTokens) null else castedTokens.requireNoNulls()
    }

    private fun castNumeral(type: ParamType.Number, token: String): Number? = when (type) {
        ParamType.Number.ByteParam -> token.toByteOrNull()
        ParamType.Number.ShortParam -> token.toShortOrNull()
        ParamType.Number.IntParam -> token.toIntOrNull()
        ParamType.Number.LongParam -> token.toLongOrNull()
        ParamType.Number.DoubleParam -> token.toDoubleOrNull()
        ParamType.Number.FloatParam -> token.toFloatOrNull()
    }

    private fun castOther(type: ParamType.Other, token: String): Any? = when (type) {
        ParamType.Other.StringParam -> if (token.any { it.isWhitespace() }) null else token
        ParamType.Other.CharParam -> token.singleOrNull()
        ParamType.Other.BooleanParam -> token.toBooleanOrNull()
    }

    private fun String.toBooleanOrNull() = if (this == "true" || this == "false") toBoolean() else null
}

internal sealed class ParamType(val name: String, val signature: Regex) {
    sealed class Number(name: String, signature: Regex) : ParamType(name, signature) {
        object ByteParam : Number("Byte", "[+-]?\\d{1,3}?".toRegex())
        object ShortParam : Number("Short", "[+-]?\\d{1,5}?".toRegex())
        object IntParam : Number("Int", "[+-]?\\d{1,10}?".toRegex())
        object LongParam : Number("Long", "[+-]?\\d{1,19}?L?".toRegex())
        object FloatParam : Number("Float", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?[fF]?".toRegex())
        object DoubleParam : Number("Double", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?".toRegex())

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

    sealed class Other(name: String, signature: Regex) : ParamType(name, signature) {
        object StringParam : Other("String", "\\S+".toRegex())
        object BooleanParam : Other("Boolean", "true|false".toRegex())
        object CharParam : Other("Char", "\\S".toRegex())

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
            in Number.typeAssociations -> Number.typeAssociations[type]
            in Other.typeAssociations -> Other.typeAssociations[type]
            else -> null
        }
    }
}

internal fun List<ParamType>.signature() = if (isEmpty()) "" else joinToString(" ") { "(${it.signature.pattern})" }
