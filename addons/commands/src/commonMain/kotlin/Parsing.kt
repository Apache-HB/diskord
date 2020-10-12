package com.serebit.strife.commands

internal class Parser {
    fun parse(message: String, signature: Regex, tokenTypes: List<ParamType<*>>): List<Any>? =
        tokenizeOrNull(message, signature)?.let { parseTokensOrNull(it, tokenTypes) }

    private fun tokenizeOrNull(message: String, signature: Regex): List<String>? =
        signature.find(message)?.groups?.mapNotNull { it?.value }?.drop(3)

    private fun parseTokensOrNull(tokens: List<String>, tokenTypes: List<ParamType<*>>): List<Any>? {
        if (tokens.isEmpty() && tokenTypes.isEmpty()) return emptyList()

        require(tokenTypes.size == tokens.size) {
            "Token types $tokenTypes has a different list size than tokens $tokens"
        }
        val castedTokens = tokenTypes.zip(tokens).map { (type, token) -> type.parse(token) }
        return castedTokens.takeUnless { null in it }?.requireNoNulls()
    }
}
