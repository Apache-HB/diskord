package com.serebit.strife.commands

import kotlin.reflect.KClass

@PublishedApi
internal object Parser {
    fun tokenize(message: String, signature: Regex): List<String>? =
        signature.find(message)?.groups?.mapNotNull { it?.value }?.drop(1)

    fun parseTokens(tokens: List<String>, tokenTypes: List<TokenType>): List<Any>? =
        if (tokens.isNotEmpty()) castTokens(tokenTypes, tokens.takeLast(tokenTypes.size)) else null

    private fun castTokens(types: List<TokenType>, tokens: List<String>): List<Any>? {
        val castedTokens = types.zip(tokens).map { (type, token) ->
            when (type) {
                is TokenType.Number -> castNumeral(type, token)
                is TokenType.Other -> castOther(type, token)
            }
        }
        return if (null in castedTokens) null else castedTokens.requireNoNulls()
    }

    private fun castNumeral(type: TokenType.Number, token: String): Number? = when (type) {
        TokenType.Number.ByteToken -> token.toByteOrNull()
        TokenType.Number.ShortToken -> token.toShortOrNull()
        TokenType.Number.IntToken -> token.toIntOrNull()
        TokenType.Number.LongToken -> token.toLongOrNull()
        TokenType.Number.DoubleToken -> token.toDoubleOrNull()
        TokenType.Number.FloatToken -> token.toFloatOrNull()
    }

    private fun castOther(type: TokenType.Other, token: String): Any? = when (type) {
        TokenType.Other.StringToken -> if (token.any { it.isWhitespace() }) null else token
        TokenType.Other.CharToken -> token.singleOrNull()
        TokenType.Other.BooleanToken -> token.toBooleanOrNull()
    }

    private fun String.toBooleanOrNull() = if (this == "true" || this == "false") toBoolean() else null
}

@PublishedApi
internal sealed class TokenType(val name: String, val signature: Regex) {
    sealed class Number(name: String, signature: Regex) : TokenType(name, signature) {
        object ByteToken : Number("Byte", "[+-]?\\d{1,3}?".toRegex())
        object ShortToken : Number("Short", "[+-]?\\d{1,5}?".toRegex())
        object IntToken : Number("Int", "[+-]?\\d{1,10}?".toRegex())
        object LongToken : Number("Long", "[+-]?\\d{1,19}?L?".toRegex())
        object FloatToken : Number("Float", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?[fF]?".toRegex())
        object DoubleToken : Number("Double", "[+-]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE]\\d+)?".toRegex())

        companion object {
            val typeAssociations = mapOf(
                Byte::class to ByteToken,
                Short::class to ShortToken,
                Int::class to IntToken,
                Long::class to LongToken,
                Float::class to FloatToken,
                Double::class to DoubleToken
            )
        }
    }

    sealed class Other(name: String, signature: Regex) : TokenType(name, signature) {
        object StringToken : Other("String", "\\S+".toRegex())
        object BooleanToken : Other("Boolean", "true|false".toRegex())
        object CharToken : Other("Char", "\\S".toRegex())

        companion object {
            val typeAssociations = mapOf(
                String::class to StringToken,
                Boolean::class to BooleanToken,
                Char::class to CharToken
            )
        }
    }

    companion object {
        operator fun invoke(type: KClass<out Any>): TokenType? = when (type) {
            in Number.typeAssociations -> Number.typeAssociations[type]
            in Other.typeAssociations -> Other.typeAssociations[type]
            else -> null
        }
    }
}

@PublishedApi
internal fun List<TokenType>.signature() = if (isEmpty()) "" else joinToString(" ") { "(${it.signature.pattern})" }
