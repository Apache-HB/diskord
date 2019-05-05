package com.serebit.strife.commands

internal class Parser {
    fun parse(message: String, signature: Regex, tokenTypes: List<ParamType>): List<Any>? =
        tokenize(message, signature)?.let { parseTokensOrNull(it, tokenTypes) }

    private fun tokenize(message: String, signature: Regex): List<String>? =
        signature.find(message)?.groups?.mapNotNull { it?.value }?.drop(1)

    private fun parseTokensOrNull(tokens: List<String>, tokenTypes: List<ParamType>): List<Any>? =
        if (tokens.isNotEmpty()) castTokens(tokenTypes, tokens.takeLast(tokenTypes.size)) else null

    private fun castTokens(types: List<ParamType>, tokens: List<String>): List<Any>? {
        val castedTokens = types.zip(tokens).map { (type, token) ->
            when (type) {
                is ParamType.NumberType<*> -> castNumeral(type, token)
                is ParamType.OtherType<*> -> castOther(type, token)
            }
        }
        return if (null in castedTokens) null else castedTokens.requireNoNulls()
    }

    private fun castNumeral(type: ParamType.NumberType<*>, token: String): Number? = when (type) {
        ParamType.NumberType.ByteParam -> token.toByteOrNull()
        ParamType.NumberType.ShortParam -> token.toShortOrNull()
        ParamType.NumberType.IntParam -> token.toIntOrNull()
        ParamType.NumberType.LongParam -> token.toLongOrNull()
        ParamType.NumberType.DoubleParam -> token.toDoubleOrNull()
        ParamType.NumberType.FloatParam -> token.toFloatOrNull()
    }

    private fun castOther(type: ParamType.OtherType<*>, token: String): Any? = when (type) {
        ParamType.OtherType.StringParam -> if (token.any { it.isWhitespace() }) null else token
        ParamType.OtherType.CharParam -> token.singleOrNull()
        ParamType.OtherType.BooleanParam -> token.toBooleanOrNull()
    }

    private fun String.toBooleanOrNull() = if (this == "true" || this == "false") toBoolean() else null
}
