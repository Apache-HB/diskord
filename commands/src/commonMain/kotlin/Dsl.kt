package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotBuilderDsl
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.onMessage

@BotBuilderDsl
fun BotBuilder.command(name: String, task: suspend MessageCreatedEvent.() -> Unit) {
    val signature = "!(\\Q$name\\E)".toRegex()
    onMessage {
        if (Parser.tokenize(message.content, signature)?.isNullOrEmpty() == false) task()
    }
}

@BotBuilderDsl
inline fun <reified T : Any> BotBuilder.command(
    name: String,
    noinline task: suspend MessageCreatedEvent.(T) -> Unit
) {
    val tokenTypes = listOf(T::class).map { TokenType(it) }.requireNoNulls()
    val paramSignature = buildString { tokenTypes.signature().let { if (it.isNotBlank()) append(" $it") } }
    val signature = "!(\\Q$name\\E)$paramSignature".toRegex()
    onMessage {
        Parser.tokenize(message.content, signature)?.let {
            Parser.parseTokens(it, tokenTypes)?.let {
                (task as suspend MessageCreatedEvent.(Any) -> Unit).invoke(this, it.single())
            }
        }
    }
}
